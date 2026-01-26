package com.capstone.personalityTest.dto.ResponseDTO.test;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Model Service Response
 * Response from model-service.py after prediction
 * 
 * Example:
 * {
 *   "predictedCode": "R-I-A"
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModelResponse {
    /**
     * The predicted personality code
     * Format: "X-Y-Z" where X, Y, Z are single letters (R, I, A, S, E, C)
     * Example: "R-I-A"
     */
    private String predictedCode;
}