package com.capstone.personalityTest.dto.ResponseDTO.Dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FinancialAidAnalyticsResponse {
    private Long totalRequests;
    private Map<String, Long> requestsByUniversity; // University name -> count
    private Map<String, Long> requestsByMajor; // Field of study/major -> count
    private Map<String, Long> requestsByStatus; // Status -> count
}
