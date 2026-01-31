package com.capstone.personalityTest.dto.ResponseDTO.Dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestAnalyticsResponse {
    private long totalAttempts;
    private Map<String, Long> attemptsByBaseTestType;
}
