package com.capstone.personalityTest.dto.ResponseDTO.Dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExhibitionOverviewResponse {
    private Long totalExhibitions;
    private Long activeExhibitions;
    private Long completedExhibitions;
    private Long cancelledExhibitions;
    private Map<String, Long> statusBreakdown; // Status name -> count
    private BigDecimal totalRevenue;
    private BigDecimal totalExpenses;
    private BigDecimal netProfit;
}
