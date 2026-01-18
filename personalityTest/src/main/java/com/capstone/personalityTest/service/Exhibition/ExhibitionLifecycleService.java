package com.capstone.personalityTest.service.Exhibition;

import com.capstone.personalityTest.model.Enum.Exhibition.ExhibitionStatus;
import com.capstone.personalityTest.model.Enum.Exhibition.ParticipationStatus;
import com.capstone.personalityTest.model.Exhibition.Exhibition;
import com.capstone.personalityTest.model.UserInfo;
import com.capstone.personalityTest.repository.Exhibition.ExhibitionRepository;
import com.capstone.personalityTest.repository.Exhibition.SchoolParticipationRepository;
import com.capstone.personalityTest.repository.Exhibition.UniversityParticipationRepository;
import com.capstone.personalityTest.repository.UserInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class ExhibitionLifecycleService {

    private final ExhibitionRepository exhibitionRepository;
    private final UniversityParticipationRepository universityParticipationRepository;
    private final SchoolParticipationRepository schoolParticipationRepository;
    private final UserInfoRepository userInfoRepository;

    // ----------------- Start Exhibition -----------------
    public Exhibition startExhibition(Long exhibitionId, String orgOwnerEmail) {
        UserInfo orgOwner = userInfoRepository.findByEmail(orgOwnerEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Exhibition exhibition = exhibitionRepository.findById(exhibitionId)
                .orElseThrow(() -> new RuntimeException("Exhibition not found"));

        // Only the org owner can start
        if (!exhibition.getOrganization().getOwner().getId().equals(orgOwner.getId())) {
            throw new RuntimeException("Only the organization owner can start the exhibition");
        }

        // Validation: Must be CONFIRMED
        if (exhibition.getStatus() != ExhibitionStatus.CONFIRMED) {
            throw new RuntimeException("Exhibition must be CONFIRMED before starting");
        }

        // Validation: At least one confirmed university and school
        boolean hasConfirmedUni = universityParticipationRepository
                .existsByExhibitionIdAndStatus(exhibitionId, ParticipationStatus.CONFIRMED);

        boolean hasConfirmedSchool = schoolParticipationRepository
                .existsByExhibitionIdAndStatus(exhibitionId, ParticipationStatus.CONFIRMED);

        if (!hasConfirmedUni && !hasConfirmedSchool) {
            throw new RuntimeException("Cannot start exhibition without confirmed universities or schools");
        }

        // Set status to ACTIVE and timestamps
        exhibition.setStatus(ExhibitionStatus.ACTIVE);
        if (exhibition.getStartDate() == null) exhibition.setStartDate(LocalDate.now());
        if (exhibition.getStartTime() == null) exhibition.setStartTime(LocalTime.now());
        exhibition.setUpdatedAt(LocalDateTime.now());

        return exhibitionRepository.save(exhibition);
    }
}
