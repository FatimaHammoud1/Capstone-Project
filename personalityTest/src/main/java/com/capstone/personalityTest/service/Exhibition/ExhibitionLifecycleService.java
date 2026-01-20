package com.capstone.personalityTest.service.Exhibition;

import com.capstone.personalityTest.model.Enum.Exhibition.ExhibitionStatus;
import com.capstone.personalityTest.model.Enum.Exhibition.ParticipationStatus;
import com.capstone.personalityTest.model.Exhibition.Exhibition;
import com.capstone.personalityTest.model.UserInfo;
import com.capstone.personalityTest.repository.Exhibition.ExhibitionRepository;
import com.capstone.personalityTest.repository.Exhibition.SchoolParticipationRepository;
import com.capstone.personalityTest.repository.Exhibition.UniversityParticipationRepository;
import com.capstone.personalityTest.repository.Exhibition.ActivityProviderRequestRepository;
import com.capstone.personalityTest.repository.UserInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.capstone.personalityTest.dto.ResponseDTO.Exhibition.ExhibitionResponse;

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

    private final ActivityProviderRequestRepository activityProviderRequestRepository;

    // ----------------- Start Exhibition -----------------
    public ExhibitionResponse startExhibition(Long exhibitionId, String orgOwnerEmail) {
        UserInfo orgOwner = userInfoRepository.findByEmail(orgOwnerEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Exhibition exhibition = exhibitionRepository.findById(exhibitionId)
                .orElseThrow(() -> new RuntimeException("Exhibition not found"));

        // Only the org owner can start, OR developer
        boolean isDev = orgOwner.getRoles().stream().anyMatch(r -> r.getCode().equals("DEVELOPER"));
        if (!exhibition.getOrganization().getOwner().getId().equals(orgOwner.getId()) && !isDev) {
            throw new RuntimeException("Only the organization owner can start the exhibition");
        }

        // Validation: Must be CONFIRMED
        if (exhibition.getStatus() != ExhibitionStatus.CONFIRMED) {
            throw new RuntimeException("Exhibition must be CONFIRMED before starting");
        }

        // Validation: At least one FINALIZED university OR activity provider
        boolean hasFinalizedUni = universityParticipationRepository
                .existsByExhibitionIdAndStatus(exhibitionId, ParticipationStatus.FINALIZED);

        boolean hasFinalizedProvider = activityProviderRequestRepository
                .existsByExhibitionIdAndStatus(exhibitionId, com.capstone.personalityTest.model.Enum.Exhibition.ActivityProviderRequestStatus.FINALIZED);

        if (!hasFinalizedUni && !hasFinalizedProvider) {
            throw new RuntimeException("Cannot start exhibition. At least one university or activity provider must have FINALIZED participation.");
        }

        // Set status to ACTIVE and timestamps
        exhibition.setStatus(ExhibitionStatus.ACTIVE);
        if (exhibition.getStartDate() == null) exhibition.setStartDate(LocalDate.now());
        if (exhibition.getStartTime() == null) exhibition.setStartTime(LocalTime.now());
        exhibition.setUpdatedAt(LocalDateTime.now());

        Exhibition savedExhibition = exhibitionRepository.save(exhibition);
        return mapToResponse(savedExhibition);
    }
    
    // 5️⃣ Add Exhibition COMPLETED status
    public ExhibitionResponse completeExhibition(Long exhibitionId, String orgOwnerEmail) {
        UserInfo orgOwner = userInfoRepository.findByEmail(orgOwnerEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Exhibition exhibition = exhibitionRepository.findById(exhibitionId)
                .orElseThrow(() -> new RuntimeException("Exhibition not found"));

        boolean isDev = orgOwner.getRoles().stream().anyMatch(r -> r.getCode().equals("DEVELOPER"));
        if (!exhibition.getOrganization().getOwner().getId().equals(orgOwner.getId()) && !isDev) {
            throw new RuntimeException("Only the organization owner can complete the exhibition");
        }

        if (exhibition.getStatus() != ExhibitionStatus.ACTIVE) {
            throw new RuntimeException("Exhibition must be ACTIVE to be completed");
        }

        exhibition.setStatus(ExhibitionStatus.COMPLETED);
        exhibition.setUpdatedAt(LocalDateTime.now());

        Exhibition savedExhibition = exhibitionRepository.save(exhibition);
        return mapToResponse(savedExhibition);
    }

    private ExhibitionResponse mapToResponse(Exhibition exhibition) {
        return new ExhibitionResponse(
            exhibition.getId(),
            exhibition.getOrganization().getId(),
            exhibition.getTitle(),
            exhibition.getDescription(),
            exhibition.getTheme(),
            exhibition.getStatus(),
            exhibition.getStartDate(),
            exhibition.getEndDate(),
            exhibition.getStartTime(),
            exhibition.getEndTime(),
            exhibition.getTotalAvailableBooths(),
            exhibition.getStandardBoothSqm(),
            exhibition.getMaxBoothsPerUniversity(),
            exhibition.getMaxBoothsPerProvider(),
            exhibition.getExpectedVisitors(),
            exhibition.getActualVisitors(),
            exhibition.getScheduleJson(),
            exhibition.getCreatedAt(),
            exhibition.getUpdatedAt(),
            exhibition.getFinalizationDeadline()
        );
    }
}
