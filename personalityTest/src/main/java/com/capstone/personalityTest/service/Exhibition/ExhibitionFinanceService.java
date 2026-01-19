package com.capstone.personalityTest.service.Exhibition;

import com.capstone.personalityTest.model.Enum.Exhibition.ActivityProviderRequestStatus;
import com.capstone.personalityTest.model.Enum.Exhibition.ExhibitionStatus;
import com.capstone.personalityTest.model.Enum.Exhibition.ParticipationStatus;
import com.capstone.personalityTest.model.Enum.Exhibition.PaymentStatus;
import com.capstone.personalityTest.model.Exhibition.ActivityProviderRequest;
import com.capstone.personalityTest.model.Exhibition.Exhibition;
import com.capstone.personalityTest.model.Exhibition.ExhibitionFinancial;
import com.capstone.personalityTest.model.Exhibition.SchoolParticipation;
import com.capstone.personalityTest.model.Exhibition.UniversityParticipation;
import com.capstone.personalityTest.model.UserInfo;
import com.capstone.personalityTest.repository.Exhibition.*;
import com.capstone.personalityTest.repository.UserInfoRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ExhibitionFinanceService {

    private final ExhibitionRepository exhibitionRepository;
    private final UniversityParticipationRepository universityParticipationRepository;
    private final SchoolParticipationRepository schoolParticipationRepository;
    private final ActivityProviderRequestRepository activityProviderRequestRepository;
    private final ExhibitionFinancialRepository exhibitionFinancialRepository;
    private final UserInfoRepository userInfoRepository;

    // ----------------- Calculate Payments & Generate Schedule -----------------
    // ----------------- Confirm Exhibition (Finalize) -----------------
    public com.capstone.personalityTest.dto.ResponseDTO.Exhibition.ExhibitionResponse confirmExhibition(Long exhibitionId, String orgOwnerEmail) {
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

        // Check Unis: Must be CONFIRMED and PAID (unless invited status ignored? usually we check active participants)
        // If a uni is Cancelled/Invited/Registered but not finalized, should we block? 
        // Logic: All "Accepted" participants must proceed to Confirmed. Any stuck in "Invited"/"Registered" might be ignored or block.
        // Sticking to: All *universities that are not cancelled* must be CONFIRMED and PAID.
        boolean allActiveUnisReady = unis.stream()
                .filter(u -> u.getStatus() != ParticipationStatus.CANCELLED)
                .allMatch(u -> u.getStatus() == ParticipationStatus.CONFIRMED && u.getPaymentStatus() == PaymentStatus.PAID);

        boolean allActiveSchoolsReady = schools.stream()
                .filter(s -> s.getStatus() != ParticipationStatus.CANCELLED && s.getStatus() != ParticipationStatus.REJECTED)
                .allMatch(s -> s.getStatus() == ParticipationStatus.ACCEPTED); // Schools are ACCEPTED by Org (or REJECTED), not CONFIRMED status enum

        if (!allActiveUnisReady || !allActiveSchoolsReady) {
            throw new RuntimeException("All active universities must be PREPAYED & CONFIRMED, and schools ACCEPTED, before finalizing");
        }

        // ----------------- Calculate Financials -----------------
        BigDecimal totalRevenue = unis.stream()
                .filter(u -> u.getStatus() == ParticipationStatus.CONFIRMED)
                .map(UniversityParticipation::getParticipationFee)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpenses = activityProviderRequestRepository
                .findByExhibitionIdAndStatus(exhibitionId, ActivityProviderRequestStatus.APPROVED)
                .stream()
                .map(ActivityProviderRequest::getTotalCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        ExhibitionFinancial financial = new ExhibitionFinancial();
        financial.setExhibition(exhibition);
        financial.setTotalRevenue(totalRevenue);
        financial.setTotalExpenses(totalExpenses);
        financial.setNetProfit(totalRevenue.subtract(totalExpenses));
        financial.setCalculatedAt(LocalDateTime.now());

        exhibitionFinancialRepository.save(financial);

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

        // Convert to JSON string (can use ObjectMapper)
        try {
            ObjectMapper mapper = new ObjectMapper();
            // Need to handle potential recursion or circular references if entities are directly serialized
            // Registering JavaTimeModule might be needed for LocalDate/Time serialization if not configured globally
            mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
            
            // Or better, map to simple DTOs to avoid full entity recursion issues in the JSON column
            // For now, let's assume entities serialize okay or are simple enough. 
            // Ideally we'd map to Light DTOs here.
            
            String scheduleJson = mapper.writeValueAsString(schedule);
            exhibition.setScheduleJson(scheduleJson);
        } catch (JsonProcessingException e) {
             // Log error but maybe don't fail the whole confirmation if just JSON gen fails? 
             // Or throw to ensure data integrity.
            throw new RuntimeException("Failed to generate schedule JSON", e);
        }

        // State Transition
        exhibition.setStatus(ExhibitionStatus.CONFIRMED);
        exhibition.setUpdatedAt(LocalDateTime.now());

        Exhibition savedExhibition = exhibitionRepository.save(exhibition);
        
        // Map to DTO
        return new com.capstone.personalityTest.dto.ResponseDTO.Exhibition.ExhibitionResponse(
                savedExhibition.getId(),
                savedExhibition.getOrganization().getId(),
                savedExhibition.getTitle(),
                savedExhibition.getDescription(),
                savedExhibition.getTheme(),
                savedExhibition.getStatus(),
                savedExhibition.getStartDate(),
                savedExhibition.getEndDate(),
                savedExhibition.getStartTime(),
                savedExhibition.getEndTime(),
                savedExhibition.getMaxCapacity(),
                savedExhibition.getExpectedVisitors(),
                savedExhibition.getScheduleJson(),
                savedExhibition.getCreatedAt(),
                savedExhibition.getUpdatedAt()
        );
    }
}
