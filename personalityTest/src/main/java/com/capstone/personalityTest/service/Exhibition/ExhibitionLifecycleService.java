package com.capstone.personalityTest.service.Exhibition;

import com.capstone.personalityTest.model.Enum.Exhibition.ActivityProviderRequestStatus;
import com.capstone.personalityTest.model.Enum.Exhibition.ExhibitionStatus;
import com.capstone.personalityTest.model.Enum.Exhibition.ParticipationStatus;
import com.capstone.personalityTest.model.Enum.Exhibition.PaymentStatus;
import com.capstone.personalityTest.model.Exhibition.Exhibition;
import com.capstone.personalityTest.model.Exhibition.SchoolParticipation;
import com.capstone.personalityTest.model.Exhibition.UniversityParticipation;
import com.capstone.personalityTest.model.UserInfo;
import com.capstone.personalityTest.repository.Exhibition.ExhibitionRepository;
import com.capstone.personalityTest.repository.Exhibition.SchoolParticipationRepository;
import com.capstone.personalityTest.repository.Exhibition.UniversityParticipationRepository;
import com.capstone.personalityTest.repository.Exhibition.ActivityProviderRequestRepository;
import com.capstone.personalityTest.repository.UserInfoRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.capstone.personalityTest.dto.ResponseDTO.Exhibition.ExhibitionResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ExhibitionLifecycleService {

    private final ExhibitionRepository exhibitionRepository;
    private final UniversityParticipationRepository universityParticipationRepository;
    private final SchoolParticipationRepository schoolParticipationRepository;
    private final UserInfoRepository userInfoRepository;
    private final ActivityProviderRequestRepository activityProviderRequestRepository;
    private final ExhibitionFinanceService financeService;

    // ----------------- Confirm Exhibition (Status Change + Schedule Generation) -----------------
    public ExhibitionResponse confirmExhibition(Long exhibitionId, LocalDateTime finalizationDeadline, String orgOwnerEmail) {
        UserInfo orgOwner = userInfoRepository.findByEmail(orgOwnerEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Exhibition exhibition = exhibitionRepository.findById(exhibitionId)
                .orElseThrow(() -> new RuntimeException("Exhibition not found"));

        // Only owner of the organization, OR developer (for easier testing)
        boolean isDev = orgOwner.getRoles().stream().anyMatch(r -> r.getCode().equals("DEVELOPER"));
        if (!exhibition.getOrganization().getOwner().getId().equals(orgOwner.getId()) && !isDev) {
            throw new RuntimeException("Only ORG_OWNER can confirm the exhibition");
        }

        // Validate that all universities and schools are confirmed
        List<UniversityParticipation> unis = universityParticipationRepository
                .findByExhibitionId(exhibitionId);

        List<SchoolParticipation> schools = schoolParticipationRepository
                .findByExhibitionId(exhibitionId);

        // Check Unis: Must be CONFIRMED and PAID
        boolean allActiveUnisReady = unis.stream()
                .filter(u -> u.getStatus() != ParticipationStatus.CANCELLED)
                .allMatch(u -> u.getStatus() == ParticipationStatus.CONFIRMED && u.getPaymentStatus() == PaymentStatus.PAID);

        boolean allActiveSchoolsReady = schools.stream()
                .filter(s -> s.getStatus() != ParticipationStatus.CANCELLED && s.getStatus() != ParticipationStatus.REJECTED)
                .allMatch(s -> s.getStatus() == ParticipationStatus.ACCEPTED);

        if (!allActiveUnisReady || !allActiveSchoolsReady) {
            throw new RuntimeException("All active universities must be PREPAYED & CONFIRMED, and schools ACCEPTED, before finalizing");
        }

        // ----------------- Generate Schedule JSON -----------------
        Map<String, Object> schedule = new HashMap<>();
        schedule.put("activities", activityProviderRequestRepository
                .findByExhibitionIdAndStatus(exhibitionId, ActivityProviderRequestStatus.APPROVED));
        schedule.put("universities", unis.stream()
                .filter(u -> u.getStatus() == ParticipationStatus.CONFIRMED)
                .toList());
        schedule.put("schools", schools.stream()
                .filter(s -> s.getStatus() == ParticipationStatus.ACCEPTED)
                .toList());

        // Convert to JSON string
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
            
            String scheduleJson = mapper.writeValueAsString(schedule);
            exhibition.setScheduleJson(scheduleJson);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to generate schedule JSON", e);
        }

        // State Transition
        exhibition.setStatus(ExhibitionStatus.CONFIRMED);
        exhibition.setFinalizationDeadline(finalizationDeadline);
        exhibition.setUpdatedAt(LocalDateTime.now());

        Exhibition savedExhibition = exhibitionRepository.save(exhibition);
        
        // Automatically calculate financials after confirmation
        financeService.calculateFinancials(exhibitionId);
        
        return mapToResponse(savedExhibition);
    }

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
