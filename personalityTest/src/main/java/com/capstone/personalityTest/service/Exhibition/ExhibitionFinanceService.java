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
    public Exhibition confirmExhibition(Long exhibitionId, String orgOwnerEmail) {
        UserInfo orgOwner = userInfoRepository.findByEmail(orgOwnerEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Exhibition exhibition = exhibitionRepository.findById(exhibitionId)
                .orElseThrow(() -> new RuntimeException("Exhibition not found"));

        if (!exhibition.getOrganization().getOwner().getId().equals(orgOwner.getId())) {
            throw new RuntimeException("Only ORG_OWNER can confirm the exhibition");
        }

        // Validate that all universities and schools are confirmed
        List<UniversityParticipation> unis = universityParticipationRepository
                .findByExhibitionId(exhibitionId);

        List<SchoolParticipation> schools = schoolParticipationRepository
                .findByExhibitionId(exhibitionId);

        boolean allUnisConfirmed = unis.stream()
                .allMatch(u -> u.getStatus() == ParticipationStatus.CONFIRMED && u.getPaymentStatus() == PaymentStatus.PAID);

        boolean allSchoolsConfirmed = schools.stream()
                .allMatch(s -> s.getStatus() == ParticipationStatus.CONFIRMED);

        if (!allUnisConfirmed || !allSchoolsConfirmed) {
            throw new RuntimeException("All universities must have paid & confirmed, and all schools confirmed, before finalizing");
        }

        // ----------------- Calculate Payments -----------------
        BigDecimal totalRevenue = unis.stream()
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
        schedule.put("universities", unis);
        schedule.put("schools", schools);

        // Convert to JSON string (can use ObjectMapper)
        try {
            ObjectMapper mapper = new ObjectMapper();
            String scheduleJson = mapper.writeValueAsString(schedule);
            exhibition.setScheduleJson(scheduleJson);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to generate schedule JSON", e);
        }

        exhibition.setStatus(ExhibitionStatus.CONFIRMED);
        exhibition.setUpdatedAt(LocalDateTime.now());

        return exhibitionRepository.save(exhibition);
    }
}
