package com.capstone.personalityTest.service.Exhibition;

import com.capstone.personalityTest.model.Enum.Exhibition.ExhibitionStatus;
import com.capstone.personalityTest.model.Enum.Exhibition.ParticipationStatus;
import com.capstone.personalityTest.model.Exhibition.Exhibition;
import com.capstone.personalityTest.model.Exhibition.School;
import com.capstone.personalityTest.model.Exhibition.SchoolParticipation;
import com.capstone.personalityTest.model.UserInfo;
import com.capstone.personalityTest.repository.Exhibition.ExhibitionRepository;
import com.capstone.personalityTest.repository.Exhibition.SchoolParticipationRepository;
import com.capstone.personalityTest.repository.Exhibition.SchoolRepository;
import com.capstone.personalityTest.repository.UserInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SchoolParticipationService {

    private final ExhibitionRepository exhibitionRepository;
    private final SchoolRepository schoolRepository;
    private final SchoolParticipationRepository participationRepository;
    private final UserInfoRepository userInfoRepository;

    // ----------------- Invite School -----------------
    public com.capstone.personalityTest.dto.ResponseDTO.Exhibition.SchoolParticipationResponse inviteSchool(Long exhibitionId, Long schoolId, LocalDateTime responseDeadline, String inviterEmail) {
        UserInfo inviter = userInfoRepository.findByEmail(inviterEmail)
                .orElseThrow(() -> new RuntimeException("Inviter not found"));

        Exhibition exhibition = exhibitionRepository.findById(exhibitionId)
                .orElseThrow(() -> new RuntimeException("Exhibition not found"));

        // Only ORG_OWNER or DEVELOPER
        boolean isDev = inviter.getRoles().stream().anyMatch(r -> r.getCode().equals("DEVELOPER"));
        if (!exhibition.getOrganization().getOwner().getId().equals(inviter.getId()) && !isDev) {
            throw new RuntimeException("Only ORG_OWNER can invite schools");
        }
        
        // Guard: Locked if CONFIRMED or later
        if (exhibition.getStatus().ordinal() >= ExhibitionStatus.CONFIRMED.ordinal()) {
            throw new RuntimeException("Exhibition is locked");
        }

        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new RuntimeException("School not found"));

        boolean alreadyInvited = participationRepository.existsByExhibitionAndSchool(exhibition, school);
        if (alreadyInvited) {
            throw new RuntimeException("This school has already been invited for this exhibition");
        }

        // 1️⃣ Missing Exhibition status transition: SCHOOL_INVITED (only once)
        if (exhibition.getStatus() == ExhibitionStatus.UNIVERSITY_INVITED) {
            exhibition.setStatus(ExhibitionStatus.SCHOOL_INVITED);
            exhibitionRepository.save(exhibition);
        }

        SchoolParticipation participation = new SchoolParticipation();
        participation.setExhibition(exhibition);
        participation.setSchool(school);
        participation.setStatus(ParticipationStatus.INVITED);
        participation.setInvitedAt(LocalDateTime.now());
        participation.setResponseDeadline(responseDeadline);

        SchoolParticipation saved = participationRepository.save(participation);
        return mapToResponse(saved);
    }

    // ----------------- School Responds to Invitation (Accept/Reject) -----------------
    public com.capstone.personalityTest.dto.ResponseDTO.Exhibition.SchoolParticipationResponse respondToInvitation(Long participationId, boolean accept, String rejectionReason, Integer expectedStudents, String schoolEmail) {
        SchoolParticipation participation = participationRepository.findById(participationId)
                .orElseThrow(() -> new RuntimeException("Participation not found"));
        
        Exhibition exhibition = participation.getExhibition();
        // Guard: Locked if CONFIRMED or later
        if (exhibition.getStatus().ordinal() >= ExhibitionStatus.CONFIRMED.ordinal()) {
             throw new RuntimeException("Exhibition is locked");
        }

        UserInfo user = userInfoRepository.findByEmail(schoolEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        boolean isDev = user.getRoles().stream().anyMatch(r -> r.getCode().equals("DEVELOPER"));

        // Corrected check: Validate against School Owner
        if (!participation.getSchool().getOwner().getId().equals(user.getId()) && !isDev) {
            throw new RuntimeException("You are not authorized to respond to this invitation");
        }

        if (participation.getStatus() != ParticipationStatus.INVITED) {
            throw new RuntimeException("Only INVITED schools can respond to invitation");
        }

        if (accept) {
            if (expectedStudents == null || expectedStudents <= 0) {
                throw new RuntimeException("Expected students count is required when accepting invitation");
            }
            participation.setExpectedStudents(expectedStudents);
            participation.setStatus(ParticipationStatus.REGISTERED); // Changed from ACCEPTED to REGISTERED
            participation.setRegisteredAt(LocalDateTime.now()); // Assuming we have registeredAt or re-use acceptedAt field logic?
            // Entity has acceptedAt, invitedAt, confirmedAt. Let's use acceptedAt as the timestamp for this step or registeredAt if it exists?
            // Checking fields: It has invitedAt, acceptedAt, confirmedAt. REGISTERED status usually implies "Signed Up".
            // Let's set acceptedAt as the time of this action (School Response).
            participation.setAcceptedAt(LocalDateTime.now()); 
        } else {
            if (rejectionReason == null || rejectionReason.trim().isEmpty()) {
                throw new RuntimeException("Rejection reason is required when declining invitation");
            }
            participation.setRejectionReason(rejectionReason);
            participation.setStatus(ParticipationStatus.CANCELLED); // Using CANCELLED for rejected invitations by School
        }

        SchoolParticipation saved = participationRepository.save(participation);
        return mapToResponse(saved);
    }

    // ----------------- Confirm Participation (By Org - Approval/Rejection) -----------------
    public com.capstone.personalityTest.dto.ResponseDTO.Exhibition.SchoolParticipationResponse confirmParticipation(Long participationId, boolean approved, String confirmerEmail) {
        UserInfo inviter = userInfoRepository.findByEmail(confirmerEmail)
                .orElseThrow(() -> new RuntimeException("Confirmer not found"));

        SchoolParticipation participation = participationRepository.findById(participationId)
                .orElseThrow(() -> new RuntimeException("Participation not found"));

        Exhibition exhibition = participation.getExhibition();
        // Guard: Locked if CONFIRMED or later
        if (exhibition.getStatus().ordinal() >= ExhibitionStatus.CONFIRMED.ordinal()) {
             throw new RuntimeException("Exhibition is locked");
        }

        boolean isDev = inviter.getRoles().stream().anyMatch(r -> r.getCode().equals("DEVELOPER"));
        if (!exhibition.getOrganization().getOwner().getId().equals(inviter.getId()) && !isDev) {
            throw new RuntimeException("Only ORG_OWNER can confirm school participation");
        }

        if (participation.getStatus() != ParticipationStatus.REGISTERED) {
            throw new RuntimeException("Only REGISTERED schools can be reviewed by Org");
        }
        
        if (approved) {
             // Validate Capacity (Students are visitors)
             // Ensure total expected visitors + this school's expected students <= exhibition expectedVisitors (if that represents max capacity for visitors)
            
             // Assuming expectedVisitors field on Exhibition is the TARGET/LIMIT for visitors:
             Integer currentVisitorSum = participationRepository.findByExhibitionAndStatus(exhibition, ParticipationStatus.ACCEPTED).stream()
                      .mapToInt(SchoolParticipation::getExpectedStudents)
                      .sum();
             
             if (currentVisitorSum + participation.getExpectedStudents() > exhibition.getExpectedVisitors()) {
                  throw new RuntimeException("Confirming this school exceeds exhibition visitor capacity (" + exhibition.getExpectedVisitors() + ")");
             }

             participation.setStatus(ParticipationStatus.ACCEPTED); // Org Approves -> ACCEPTED
             participation.setConfirmedAt(LocalDateTime.now()); // Re-using confirmedAt for Org Approval time? Or stick to ACCEPTED semantic.
             // Entity has confirmedAt. Let's use that.
        } else {
            participation.setStatus(ParticipationStatus.REJECTED); // Org Rejects -> REJECTED
        }

        SchoolParticipation saved = participationRepository.save(participation);
        return mapToResponse(saved);
    }

    // ----------------- Finalize Participation (After Schedule) -----------------
    public com.capstone.personalityTest.dto.ResponseDTO.Exhibition.SchoolParticipationResponse finalizeParticipation(Long participationId, String schoolEmail) {
        UserInfo schoolUser = userInfoRepository.findByEmail(schoolEmail)
                .orElseThrow(() -> new RuntimeException("School user not found"));

        SchoolParticipation participation = participationRepository.findById(participationId)
                .orElseThrow(() -> new RuntimeException("Participation not found"));
        
        Exhibition exhibition = participation.getExhibition();

        boolean isDev = schoolUser.getRoles().stream().anyMatch(r -> r.getCode().equals("DEVELOPER"));
        if (!participation.getSchool().getOwner().getId().equals(schoolUser.getId()) && !isDev) {
            throw new RuntimeException("Only the school owner can finalize participation");
        }

        if (exhibition.getStatus() != ExhibitionStatus.CONFIRMED) {
            throw new RuntimeException("Cannot finalize participation before exhibition is CONFIRMED (schedule ready)");
        }

        if (participation.getStatus() != ParticipationStatus.ACCEPTED) {
            throw new RuntimeException("Only ACCEPTED schools can be finalized");
        }

        participation.setStatus(ParticipationStatus.FINALIZED);
        // Could set a finalizedAt timestamp if entity had it.
        
        SchoolParticipation saved = participationRepository.save(participation);
        return mapToResponse(saved);
    }
    
    // ----------------- Cancel School Participation -----------------
    @Transactional
    public com.capstone.personalityTest.dto.ResponseDTO.Exhibition.SchoolParticipationResponse cancelParticipation(Long participationId, String cancellerEmail) {
        UserInfo canceller = userInfoRepository.findByEmail(cancellerEmail)
                .orElseThrow(() -> new RuntimeException("Canceller not found"));

        SchoolParticipation participation = participationRepository.findById(participationId)
                .orElseThrow(() -> new RuntimeException("Participation not found"));

        Exhibition exhibition = participation.getExhibition();

        if (exhibition.getStatus() == ExhibitionStatus.ACTIVE) {
            throw new RuntimeException("Cannot cancel participation when exhibition is ACTIVE");
        }

        // Check if canceller is the School Owner
        boolean isSchoolOwner = participation.getSchool().getOwner().getId().equals(canceller.getId());
        boolean isOrgOwner = exhibition.getOrganization().getOwner().getId().equals(canceller.getId());

        boolean isDev = canceller.getRoles().stream().anyMatch(r -> r.getCode().equals("DEVELOPER"));

        if (!isSchoolOwner && !isOrgOwner && !isDev) {
            throw new RuntimeException("Not authorized to cancel this participation");
        }

        if (participation.getStatus() != ParticipationStatus.ACCEPTED && participation.getStatus() != ParticipationStatus.CONFIRMED) {
             throw new RuntimeException("Only ACCEPTED or CONFIRMED participations can be cancelled");
        }

        participation.setStatus(ParticipationStatus.CANCELLED);
        
        SchoolParticipation saved = participationRepository.save(participation);
        return mapToResponse(saved);
    }

    private com.capstone.personalityTest.dto.ResponseDTO.Exhibition.SchoolParticipationResponse mapToResponse(SchoolParticipation participation) {
        return new com.capstone.personalityTest.dto.ResponseDTO.Exhibition.SchoolParticipationResponse(
            participation.getId(),
            participation.getExhibition().getId(),
            participation.getSchool().getId(),
            participation.getSchool().getName(),
            participation.getSchool().getContactEmail(),
            participation.getStatus(),
            participation.getExpectedStudents(),
            participation.getResponseDeadline(),
            participation.getInvitedAt(),
            participation.getAcceptedAt(),
            participation.getRejectionReason(),
            participation.getConfirmedAt()
        );
    }
}
