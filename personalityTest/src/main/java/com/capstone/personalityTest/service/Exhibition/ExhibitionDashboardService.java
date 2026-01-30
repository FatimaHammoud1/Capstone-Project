package com.capstone.personalityTest.service.Exhibition;

import com.capstone.personalityTest.dto.ResponseDTO.Dashboard.ExhibitionOverviewResponse;
import com.capstone.personalityTest.model.Enum.Exhibition.ExhibitionStatus;
import com.capstone.personalityTest.model.Exhibition.Exhibition;
import com.capstone.personalityTest.model.Exhibition.ExhibitionFinancial;
import com.capstone.personalityTest.repository.Exhibition.ExhibitionFinancialRepository;
import com.capstone.personalityTest.repository.Exhibition.ExhibitionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExhibitionDashboardService {

    private final ExhibitionRepository exhibitionRepository;
    private final ExhibitionFinancialRepository exhibitionFinancialRepository;

    /**
     * Get exhibition overview dashboard statistics
     * @param orgId Optional organization ID filter
     * @return ExhibitionOverviewResponse with aggregated statistics
     */
    public ExhibitionOverviewResponse getExhibitionsOverview(Long orgId) {
        // Fetch exhibitions (filtered by org if provided)
        List<Exhibition> exhibitions = orgId != null 
            ? exhibitionRepository.findByOrganizationId(orgId)
            : exhibitionRepository.findAll();

        // Calculate total exhibitions
        long totalExhibitions = exhibitions.size();

        // Count exhibitions by status
        long activeExhibitions = exhibitions.stream()
            .filter(e -> e.getStatus() == ExhibitionStatus.ACTIVE)
            .count();

        long completedExhibitions = exhibitions.stream()
            .filter(e -> e.getStatus() == ExhibitionStatus.COMPLETED)
            .count();

        long cancelledExhibitions = exhibitions.stream()
            .filter(e -> e.getStatus() == ExhibitionStatus.CANCELLED_BY_ORG || 
                         e.getStatus() == ExhibitionStatus.CANCELLED_BY_MUNICIPALITY)
            .count();

        // Create status breakdown map for all statuses
        Map<String, Long> statusBreakdown = new HashMap<>();
        for (ExhibitionStatus status : ExhibitionStatus.values()) {
            long count = exhibitions.stream()
                .filter(e -> e.getStatus() == status)
                .count();
            statusBreakdown.put(status.name(), count);
        }

        // Calculate financial totals from EXISTING financial records only
        // Note: Only exhibitions with calculated financials will be included
        // Org owners should call /api/exhibitions/{id}/calculate-financials first
        List<Long> exhibitionIds = exhibitions.stream()
            .map(Exhibition::getId)
            .collect(Collectors.toList());

        // Get all financial records for these exhibitions
        List<ExhibitionFinancial> financials = exhibitionFinancialRepository.findAll()
            .stream()
            .filter(f -> exhibitionIds.contains(f.getExhibition().getId()))
            .collect(Collectors.toList());

        // Sum up revenue and expenses
        BigDecimal totalRevenue = financials.stream()
            .map(ExhibitionFinancial::getTotalRevenue)
            .filter(revenue -> revenue != null)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpenses = financials.stream()
            .map(ExhibitionFinancial::getTotalExpenses)
            .filter(expenses -> expenses != null)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal netProfit = totalRevenue.subtract(totalExpenses);

        return new ExhibitionOverviewResponse(
            totalExhibitions,
            activeExhibitions,
            completedExhibitions,
            cancelledExhibitions,
            statusBreakdown,
            totalRevenue,
            totalExpenses,
            netProfit
        );
    }
}
