package com.capstone.personalityTest.service.Exhibition;

import com.capstone.personalityTest.model.Enum.Exhibition.ExhibitionStatus;
import com.capstone.personalityTest.model.Enum.Exhibition.ParticipationStatus;
import com.capstone.personalityTest.model.Enum.Exhibition.PaymentStatus;
import com.capstone.personalityTest.model.Exhibition.Exhibition;
import com.capstone.personalityTest.model.Exhibition.University;
import com.capstone.personalityTest.model.Exhibition.UniversityParticipation;
import com.capstone.personalityTest.model.UserInfo;
import com.capstone.personalityTest.repository.Exhibition.*;
import com.capstone.personalityTest.repository.UserInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import com.capstone.personalityTest.dto.ResponseDTO.Exhibition.UniversityResponse;
import java.util.stream.Collectors;
import java.util.List;
import com.capstone.personalityTest.dto.ResponseDTO.Exhibition.UniversityParticipationResponse;
import com.capstone.personalityTest.model.Exhibition.Booth;
import com.capstone.personalityTest.model.Enum.Exhibition.BoothType;

@Service
@RequiredArgsConstructor
public class UniversityParticipationService {

    private final ExhibitionRepository exhibitionRepository;
    private final UniversityRepository universityRepository;
    private final UniversityParticipationRepository participationRepository;
    private final UserInfoRepository userInfoRepository;
    private final BoothRepository boothRepository;
    private final ExhibitionService exhibitionService;

    // ----------------- Invite University -----------------
    public UniversityParticipationResponse inviteUniversity(Long exhibitionId, Long universityId, BigDecimal participationFee, LocalDateTime responseDeadline, String inviterEmail) {
        UserInfo inviter = userInfoRepository.findByEmail(inviterEmail)
                .orElseThrow(() -> new RuntimeException("Inviter not found"));

        Exhibition exhibition = exhibitionRepository.findById(exhibitionId)
                .orElseThrow(() -> new RuntimeException("Exhibition not found"));

        // Only ORG_OWNER or DEVELOPER
        boolean isDev = inviter.getRoles().stream().anyMatch(r -> r.getCode().equals("DEVELOPER"));
        if (!exhibition.getOrganization().getOwner().getId().equals(inviter.getId()) && !isDev) {
            throw new RuntimeException("Only ORG_OWNER can invite universities");
        }

        // Validate exhibition status: must be VENUE_APPROVED or PLANNING
        if (exhibition.getStatus() != ExhibitionStatus.VENUE_APPROVED && exhibition.getStatus() != ExhibitionStatus.PLANNING) {
            throw new RuntimeException("Can only invite universities after venue is approved");
        }
        
        // Transition to PLANNING if first invitation
        if (exhibition.getStatus() == ExhibitionStatus.VENUE_APPROVED) {
            exhibition.setStatus(ExhibitionStatus.PLANNING);
            exhibitionRepository.save(exhibition);
        }

        University university = universityRepository.findById(universityId)
                .orElseThrow(() -> new RuntimeException("University not found"));

        boolean alreadyInvited = participationRepository.existsByExhibitionAndUniversity(exhibition, university);
        if (alreadyInvited) {
            throw new RuntimeException("This university has already been invited for this exhibition");
        }

        UniversityParticipation participation = new UniversityParticipation();
        participation.setExhibition(exhibition);
        participation.setUniversity(university);
        participation.setStatus(ParticipationStatus.INVITED);
        participation.setParticipationFee(participationFee);
        participation.setInvitedAt(LocalDateTime.now());
        participation.setResponseDeadline(responseDeadline);
        
        // Set max booths info from exhibition (will be included in response)
        // The university will see this in the response via exhibition data

        UniversityParticipation saved = participationRepository.save(participation);
        return mapToResponse(saved);
    }

    // ----------------- University Registers -----------------
    public UniversityParticipationResponse registerUniversity(Long participationId, int requestedBooths, Map<Long, Map<String, Object>> boothDetails, String universityEmail) {
        UniversityParticipation participation = participationRepository.findById(participationId)
                .orElseThrow(() -> new RuntimeException("Participation request not found"));

        Exhibition exhibition = participation.getExhibition();
        
        // Validate exhibition status
        if (exhibition.getStatus() != ExhibitionStatus.PLANNING) {
            throw new RuntimeException("Can only register during PLANNING phase");
        }

        University university = participation.getUniversity();
        UserInfo user = userInfoRepository.findByEmail(universityEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        boolean isDev = user.getRoles().stream().anyMatch(r -> r.getCode().equals("DEVELOPER"));
        
        // Validate against University Owner
        if (!university.getOwner().getId().equals(user.getId()) && !isDev) {
            throw new RuntimeException("You are not authorized to register this university");
        }

        // Allow registration if INVITED or REJECTED (can re-register after rejection)
        if (participation.getStatus() != ParticipationStatus.INVITED && participation.getStatus() != ParticipationStatus.REJECTED) {
            throw new RuntimeException("University must be INVITED or REJECTED to register");
        }
        
        // Validate response deadline - auto-cancel if passed
        if (participation.getResponseDeadline() != null && LocalDateTime.now().isAfter(participation.getResponseDeadline())) {
            participation.setStatus(ParticipationStatus.CANCELLED);
            participationRepository.save(participation);
            throw new RuntimeException("Response deadline has passed. Your invitation has been automatically cancelled.");
        }
        
        // Validate requested booths is positive
        if (requestedBooths <= 0) {
            throw new RuntimeException("Requested booths must be positive");
        }

        // Validate per-university booth limit (if set)
        if (exhibition.getMaxBoothsPerUniversity() != null && requestedBooths > exhibition.getMaxBoothsPerUniversity()) {
            throw new RuntimeException("Requested booths (" + requestedBooths + ") exceeds max per university (" + exhibition.getMaxBoothsPerUniversity() + ")");
        }

        // Validate total available booths
        int totalExistingBooths = boothRepository.countByExhibition(exhibition);
        int remainingBooths = exhibition.getTotalAvailableBooths() - totalExistingBooths;
        
        if (requestedBooths > remainingBooths) {
            throw new RuntimeException("Requested booths (" + requestedBooths + ") exceeds remaining capacity (" + remainingBooths + ")");
        }

        // Save booth details JSON
        participation.setBoothDetails(boothDetails);
        participation.setApprovedBoothsCount(requestedBooths);
        participation.setRegisteredAt(LocalDateTime.now());
        participation.setStatus(ParticipationStatus.REGISTERED);

        UniversityParticipation saved = participationRepository.save(participation);
        return mapToResponse(saved);
    }

    // ----------------- Approve or Reject University -----------------
    public UniversityParticipationResponse reviewUniversity(Long participationId, boolean approve, LocalDateTime confirmationDeadline, String reviewerEmail) {
        UserInfo reviewer = userInfoRepository.findByEmail(reviewerEmail)
                .orElseThrow(() -> new RuntimeException("Reviewer not found"));

        UniversityParticipation participation = participationRepository.findById(participationId)
                .orElseThrow(() -> new RuntimeException("Participation not found"));

        Exhibition exhibition = participation.getExhibition();
        // Guard: Locked if CONFIRMED or later
        // Validate exhibition status
        if (exhibition.getStatus() != ExhibitionStatus.PLANNING) {
            throw new RuntimeException("Can only review university participation during PLANNING phase");
        }


        boolean isDev = reviewer.getRoles().stream().anyMatch(r -> r.getCode().equals("DEVELOPER"));
        if (!exhibition.getOrganization().getOwner().getId().equals(reviewer.getId()) && !isDev) {
            throw new RuntimeException("Only ORG_OWNER can approve/reject university participation");
        }

        if (participation.getStatus() != ParticipationStatus.REGISTERED) {
            throw new RuntimeException("Only REGISTERED universities can be reviewed");
        }

        if (approve) {
            participation.setStatus(ParticipationStatus.ACCEPTED);
            participation.setConfirmationDeadline(confirmationDeadline);
            // Create booths for the university
            if (participation.getApprovedBoothsCount() != null && participation.getApprovedBoothsCount() > 0) {
                for (int i = 0; i < participation.getApprovedBoothsCount(); i++) {
                     Booth booth = new Booth();
                     booth.setExhibition(exhibition);
                     booth.setBoothType(BoothType.UNIVERSITY);
                     booth.setUniversityParticipationId(participation.getId());
                     booth.setCreatedAt(LocalDateTime.now());
                     booth.setZone("Unassigned");
                     booth.setBoothNumber(0);
                     // University booths might not have specific durations/max participants at creation like activities do
                     // or we could default them.
                     
                     boothRepository.save(booth);
                }
            }
        } else {
            participation.setStatus(ParticipationStatus.CANCELLED); // rejected universities set as CANCELLED
        }

        UniversityParticipation saved = participationRepository.save(participation);
        return mapToResponse(saved);
    }
    
    // 3️⃣ Explicit payment confirmation (Step 2 & 3 in Goal)
    public UniversityParticipationResponse confirmPayment(Long participationId, String orgOwnerEmail) {
        UserInfo orgOwner = userInfoRepository.findByEmail(orgOwnerEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UniversityParticipation participation = participationRepository.findById(participationId)
                .orElseThrow(() -> new RuntimeException("Participation not found"));

        Exhibition exhibition = participation.getExhibition();

        // Locked if CONFIRMED? Payment usually implies pre-confirmation. 
        // Logic says "confirmExhibition" depends on everyone being paid. So confirming payment is allowed before exhibition CONFIRMED.
        if (exhibition.getStatus().ordinal() >= ExhibitionStatus.CONFIRMED.ordinal()) {
            throw new RuntimeException("Exhibition is locked");
        }

        boolean isDev = orgOwner.getRoles().stream().anyMatch(r -> r.getCode().equals("DEVELOPER"));
        if (!exhibition.getOrganization().getOwner().getId().equals(orgOwner.getId()) && !isDev) {
             throw new RuntimeException("Only ORG_OWNER can confirm payment");
        }

        // Validate confirmation deadline - auto-cancel if passed
        if (participation.getConfirmationDeadline() != null && LocalDateTime.now().isAfter(participation.getConfirmationDeadline())) {
            participation.setStatus(ParticipationStatus.CANCELLED);
            participationRepository.save(participation);
            throw new RuntimeException("Confirmation deadline has passed. Your participation has been automatically cancelled.");
        }

        participation.setPaymentStatus(PaymentStatus.PAID);
        participation.setPaymentDate(LocalDateTime.now());
        // 2️⃣ Add University CONFIRMED step
        participation.setStatus(ParticipationStatus.CONFIRMED);
        participation.setConfirmedAt(LocalDateTime.now());

        UniversityParticipation saved = participationRepository.save(participation);
        return mapToResponse(saved);
    }
    
    // ----------------- Finalize Participation (After Schedule) -----------------
    public UniversityParticipationResponse finalizeParticipation(Long participationId, String universityEmail) {
        UserInfo uniUser = userInfoRepository.findByEmail(universityEmail)
                .orElseThrow(() -> new RuntimeException("University user not found"));

        UniversityParticipation participation = participationRepository.findById(participationId)
                .orElseThrow(() -> new RuntimeException("Participation not found"));
        
        Exhibition exhibition = participation.getExhibition();

        boolean isDev = uniUser.getRoles().stream().anyMatch(r -> r.getCode().equals("DEVELOPER"));
        if (!participation.getUniversity().getOwner().getId().equals(uniUser.getId()) && !isDev) {
            throw new RuntimeException("Only the university owner can finalize participation");
        }

        if (exhibition.getStatus() != ExhibitionStatus.CONFIRMED) {
            throw new RuntimeException("Cannot finalize participation before exhibition is CONFIRMED (schedule ready)");
        }
        
        // Validate finalization deadline - auto-cancel if passed
        if (exhibition.getFinalizationDeadline() != null && LocalDateTime.now().isAfter(exhibition.getFinalizationDeadline())) {
            participation.setStatus(ParticipationStatus.CANCELLED);
            participationRepository.save(participation);
            throw new RuntimeException("Finalization deadline has passed. Your participation has been automatically cancelled.");
        }

        if (participation.getStatus() != ParticipationStatus.CONFIRMED) {
            throw new RuntimeException("Only CONFIRMED (Paid) universities can be finalized");
        }

        participation.setStatus(ParticipationStatus.FINALIZED);
        participation.setFinalizedAt(LocalDateTime.now());
        
        UniversityParticipation saved = participationRepository.save(participation);
        return mapToResponse(saved);
    }
    
    // ----------------- Cancel University Participation -----------------
    @Transactional
    public UniversityParticipationResponse cancelParticipation(Long participationId, String cancellerEmail) {
        UserInfo canceller = userInfoRepository.findByEmail(cancellerEmail)
                .orElseThrow(() -> new RuntimeException("Canceller not found"));

        UniversityParticipation participation = participationRepository.findById(participationId)
                .orElseThrow(() -> new RuntimeException("Participation not found"));

        Exhibition exhibition = participation.getExhibition();

        if (exhibition.getStatus() == ExhibitionStatus.ACTIVE) {
            throw new RuntimeException("Cannot cancel participation when exhibition is ACTIVE");
        }

        // Check if canceller is the University Owner
        boolean isUniOwner = participation.getUniversity().getOwner().getId().equals(canceller.getId());
        boolean isOrgOwner = exhibition.getOrganization().getOwner().getId().equals(canceller.getId());

        boolean isDev = canceller.getRoles().stream().anyMatch(r -> r.getCode().equals("DEVELOPER"));

        if (!isUniOwner && !isOrgOwner && !isDev) {
            throw new RuntimeException("Not authorized to cancel this participation");
        }

        // If payment was made, mark as refundable
        if (participation.getPaymentStatus() == PaymentStatus.PAID) {
            participation.setPaymentStatus(PaymentStatus.REFUNDED);
        }

        participation.setStatus(ParticipationStatus.CANCELLED);
        
        // Side effects: Remove university booths
        List<Booth> booths = boothRepository.findByUniversityParticipationId(participation.getId());
        boothRepository.deleteAll(booths);
        
        UniversityParticipation saved = participationRepository.save(participation);
        return mapToResponse(saved);
    }

    // ----------------- GET Participations -----------------
    public UniversityParticipationResponse getParticipationById(Long participationId) {
        UniversityParticipation participation = participationRepository.findById(participationId)
                .orElseThrow(() -> new RuntimeException("Participation not found"));
        return mapToResponse(participation);
    }

    public java.util.List<UniversityParticipationResponse> getParticipationsByExhibition(Long exhibitionId) {
        Exhibition exhibition = exhibitionRepository.findById(exhibitionId)
                .orElseThrow(() -> new RuntimeException("Exhibition not found"));

        return participationRepository.findAll().stream()
                .filter(p -> p.getExhibition().getId().equals(exhibitionId))
                .map(this::mapToResponse)
                .collect(java.util.stream.Collectors.toList());
    }

    public List<UniversityParticipationResponse> getParticipationsByUniversityId(Long universityId) {
        University university = universityRepository.findById(universityId)
                .orElseThrow(() -> new RuntimeException("University not found"));

        return participationRepository.findAll().stream()
                .filter(p -> p.getUniversity().getId().equals(universityId))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private UniversityParticipationResponse mapToResponse(UniversityParticipation participation) {
        // Safe null handling for booth details
        String boothDetailsJson = null;
        try {
             if (participation.getBoothDetails() != null) {
                 boothDetailsJson = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(participation.getBoothDetails());
             }
        } catch (Exception e) {
             // ignore failure, return null
        }

        return new UniversityParticipationResponse(
            participation.getId(),
            participation.getExhibition().getId(),
            participation.getUniversity().getId(),
            participation.getUniversity().getName(),
            participation.getUniversity().getContactEmail(),
            participation.getStatus(),
            participation.getApprovedBoothsCount(),
            boothDetailsJson,
            participation.getParticipationFee(),
            participation.getPaymentStatus() != null ? participation.getPaymentStatus().name() : null,
            participation.getPaymentDate(),
            participation.getResponseDeadline(),
            participation.getInvitedAt(),
            participation.getRegisteredAt(),
            participation.getConfirmedAt()
        );
    }
    

    // ----------------- Get All Active Universities -----------------
    // ----------------- Get All Active Universities -----------------
    public List<UniversityResponse> getAllActiveUniversities() {
        return universityRepository.findAll().stream()
                .filter(university -> Boolean.TRUE.equals(university.getActive()))
                .map(this::mapToUniversityResponse)
                .collect(Collectors.toList());
    }

    // ----------------- Get University By ID -----------------
    public UniversityResponse getUniversityById(Long universityId) {
        University university = universityRepository.findById(universityId)
                .orElseThrow(() -> new RuntimeException("University not found"));
        return mapToUniversityResponse(university);
    }
    
    // ----------------- Get Universities By Owner ID -----------------
    public List<UniversityResponse> getUniversitiesByOwnerId(Long ownerId) {
        return universityRepository.findAllByOwnerId(ownerId).stream()
                .map(this::mapToUniversityResponse)
                .collect(Collectors.toList());
    }

    private UniversityResponse mapToUniversityResponse(University university) {
        return new UniversityResponse(
                university.getId(),
                university.getName(),
                university.getContactEmail(),
                university.getContactPhone(),
                university.getActive(),
                university.getOwner() != null ? university.getOwner().getId() : null
        );
    }
}
