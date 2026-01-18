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
    public SchoolParticipation inviteSchool(Long exhibitionId, Long schoolId, LocalDateTime responseDeadline, String inviterEmail) {
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

        return participationRepository.save(participation);
    }

    // ----------------- School Accepts Invitation -----------------
    public SchoolParticipation acceptInvitation(Long participationId, String schoolEmail) {
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
            throw new RuntimeException("You are not authorized to accept this invitation");
        }

        if (participation.getStatus() != ParticipationStatus.INVITED) {
            throw new RuntimeException("Only INVITED schools can accept invitation");
        }

        participation.setStatus(ParticipationStatus.ACCEPTED);
        participation.setAcceptedAt(LocalDateTime.now());

        return participationRepository.save(participation);
    }

    // ----------------- Confirm Participation -----------------
    public SchoolParticipation confirmParticipation(Long participationId, String confirmerEmail) {
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

        if (participation.getStatus() != ParticipationStatus.ACCEPTED) {
            throw new RuntimeException("Only ACCEPTED schools can be confirmed");
        }

        participation.setStatus(ParticipationStatus.CONFIRMED);
        participation.setConfirmedAt(LocalDateTime.now());

        return participationRepository.save(participation);
    }
    
    // ----------------- Cancel School Participation -----------------
    @Transactional
    public SchoolParticipation cancelParticipation(Long participationId, String cancellerEmail) {
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
        
        return participationRepository.save(participation);
    }
}
