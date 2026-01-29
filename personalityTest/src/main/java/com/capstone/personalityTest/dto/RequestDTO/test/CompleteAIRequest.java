package com.capstone.personalityTest.dto.RequestDTO.test;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Request DTO for sending test results to Python AI service for complete analysis.
 * This is sent from Spring Boot to Python AI after calculating the personality code.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompleteAIRequest {

    /**
     * ID of the test attempt in our database
     * Used to link AI results back to the test attempt
     */
    private Long attemptId;

    /**
     * Personality code calculated by Spring Boot (e.g., "R-I-A")
     * This is the top 3 metrics from the test results
     */
    private String personalityCode;

    /**
     * Student information for personalized recommendations and email
     */
    private StudentInfoDTO studentInfo;

    /**
     * Complete metric scores from the test
     * Map of metric code (e.g., "R", "I", "A") to score
     * Example: {"R": 45, "I": 42, "A": 40, "S": 30, "E": 28, "C": 25}
     */
    private Map<String, Integer> metricScores;
}
