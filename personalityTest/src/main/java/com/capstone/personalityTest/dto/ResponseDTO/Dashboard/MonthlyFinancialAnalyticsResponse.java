package com.capstone.personalityTest.dto.ResponseDTO.Dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyFinancialAnalyticsResponse {
    
    private List<MonthlyStat> monthlyStats;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyStat {
        private String month; // Format: YYYY-MM
        private BigDecimal totalRevenue;
        private BigDecimal totalExpenses;
        private BigDecimal netProfit;
    }
}
