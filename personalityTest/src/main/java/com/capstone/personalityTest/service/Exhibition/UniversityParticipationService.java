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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UniversityParticipationService {

    private final ExhibitionRepository exhibitionRepository;
    private final UniversityRepository universityRepository;
    private final UniversityParticipationRepository participationRepository;
    private final UserInfoRepository userInfoRepository;
    private final BoothRepository boothRepository;

    // ----------------- Invite University -----------------
    public UniversityParticipation inviteUniversity(Long exhibitionId, Long universityId, BigDecimal participationFee, LocalDateTime responseDeadline, String inviterEmail) {
        UserInfo inviter = userInfoRepository.findByEmail(inviterEmail)
                .orElseThrow(() -> new RuntimeException("Inviter not found"));

        Exhibition exhibition = exhibitionRepository.findById(exhibitionId)
                .orElseThrow(() -> new RuntimeException("Exhibition not found"));

        if (!exhibition.getOrganization().getOwner().getId().equals(inviter.getId())) {
            throw new RuntimeException("Only ORG_OWNER can invite universities");
        }

        // Guard: Locked if CONFIRMED or later
        if (exhibition.getStatus().ordinal() >= ExhibitionStatus.CONFIRMED.ordinal()) {
            throw new RuntimeException("Exhibition is locked");
        }

        University university = universityRepository.findById(universityId)
                .orElseThrow(() -> new RuntimeException("University not found"));

        boolean alreadyInvited = participationRepository.existsByExhibitionAndUniversity(exhibition, university);
        if (alreadyInvited) {
            throw new RuntimeException("This university has already been invited for this exhibition");
        }
        
        // 1️⃣ Missing Exhibition status transition: UNIVERSITY_INVITED (only once)
        if (exhibition.getStatus() == ExhibitionStatus.ACTIVITY_APPROVED) {
            exhibition.setStatus(ExhibitionStatus.UNIVERSITY_INVITED);
            exhibitionRepository.save(exhibition);
        }

        UniversityParticipation participation = new UniversityParticipation();
        participation.setExhibition(exhibition);
        participation.setUniversity(university);
        participation.setStatus(ParticipationStatus.INVITED);
        participation.setParticipationFee(participationFee);
        participation.setInvitedAt(LocalDateTime.now());
        participation.setResponseDeadline(responseDeadline);

        return participationRepository.save(participation);
    }

    // ----------------- University Registers -----------------
    public UniversityParticipation registerUniversity(Long participationId, int requestedBooths, Map<Long, Map<String, Object>> boothDetails, String universityEmail) {
        UniversityParticipation participation = participationRepository.findById(participationId)
                .orElseThrow(() -> new RuntimeException("Participation request not found"));

        Exhibition exhibition = participation.getExhibition();
        // Guard: Locked if CONFIRMED or later
        if (exhibition.getStatus().ordinal() >= ExhibitionStatus.CONFIRMED.ordinal()) {
            throw new RuntimeException("Exhibition is locked");
        }

        University university = participation.getUniversity();
        if (!university.getContactEmail().equals(universityEmail)) {
            throw new RuntimeException("You are not authorized to register this university");
        }

        if (participation.getStatus() != ParticipationStatus.INVITED) {
            throw new RuntimeException("University must be INVITED to register");
        }

        // Capacity check: sum of all booths already assigned
        int totalExistingBooths = boothRepository.countByExhibition(exhibition);
        if (totalExistingBooths + requestedBooths > exhibition.getMaxCapacity()) {
            throw new RuntimeException("Requested booths exceed exhibition max capacity");
        }

        // Save booth details JSON
        participation.setBoothDetails(boothDetails);
        participation.setApprovedBoothsCount(requestedBooths);
        participation.setRegisteredAt(LocalDateTime.now());
        participation.setStatus(ParticipationStatus.REGISTERED);

        return participationRepository.save(participation);
    }

    // ----------------- Approve or Reject University -----------------
    public UniversityParticipation reviewUniversity(Long participationId, boolean approve, String reviewerEmail) {
        UserInfo reviewer = userInfoRepository.findByEmail(reviewerEmail)
                .orElseThrow(() -> new RuntimeException("Reviewer not found"));

        UniversityParticipation participation = participationRepository.findById(participationId)
                .orElseThrow(() -> new RuntimeException("Participation not found"));

        Exhibition exhibition = participation.getExhibition();
        // Guard: Locked if CONFIRMED or later
        if (exhibition.getStatus().ordinal() >= ExhibitionStatus.CONFIRMED.ordinal()) {
            throw new RuntimeException("Exhibition is locked");
        }

        if (!exhibition.getOrganization().getOwner().getId().equals(reviewer.getId())) {
            throw new RuntimeException("Only ORG_OWNER can approve/reject university participation");
        }

        if (participation.getStatus() != ParticipationStatus.REGISTERED) {
            throw new RuntimeException("Only REGISTERED universities can be reviewed");
        }

        if (approve) {
            participation.setStatus(ParticipationStatus.ACCEPTED);
        } else {
            participation.setStatus(ParticipationStatus.CANCELLED); // rejected universities set as CANCELLED
        }

        return participationRepository.save(participation);
    }
    
    // 3️⃣ Explicit payment confirmation (Step 2 & 3 in Goal)
    public UniversityParticipation confirmPayment(Long participationId, String orgOwnerEmail) {
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

        if (!exhibition.getOrganization().getOwner().getId().equals(orgOwner.getId())) {
             throw new RuntimeException("Only ORG_OWNER can confirm payment");
        }

        participation.setPaymentStatus(PaymentStatus.PAID);
        participation.setPaymentDate(LocalDateTime.now());
        // 2️⃣ Add University CONFIRMED step
        participation.setStatus(ParticipationStatus.CONFIRMED);
        participation.setConfirmedAt(LocalDateTime.now());

        return participationRepository.save(participation);
    }
}
