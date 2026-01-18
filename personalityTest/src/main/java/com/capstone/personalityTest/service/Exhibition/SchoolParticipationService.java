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

        if (!exhibition.getOrganization().getOwner().getId().equals(inviter.getId())) {
            throw new RuntimeException("Only ORG_OWNER can invite schools");
        }

        // if (exhibition.getStatus() != ExhibitionStatus.UNIVERSITY_INVITED) {
        //    throw new RuntimeException("Exhibition must have universities invited before inviting schools");
        // }

        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new RuntimeException("School not found"));

        boolean alreadyInvited = participationRepository.existsByExhibitionAndSchool(exhibition, school);
        if (alreadyInvited) {
            throw new RuntimeException("This school has already been invited for this exhibition");
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

        if (!participation.getSchool().getContactEmail().equals(schoolEmail)) {
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

        if (!exhibition.getOrganization().getOwner().getId().equals(inviter.getId())) {
            throw new RuntimeException("Only ORG_OWNER can confirm school participation");
        }

        if (participation.getStatus() != ParticipationStatus.ACCEPTED) {
            throw new RuntimeException("Only ACCEPTED schools can be confirmed");
        }

        participation.setStatus(ParticipationStatus.CONFIRMED);
        participation.setConfirmedAt(LocalDateTime.now());

        return participationRepository.save(participation);
    }
}
