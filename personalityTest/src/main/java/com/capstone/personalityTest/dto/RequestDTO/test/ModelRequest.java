package com.capstone.personalityTest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO for Model Service Request
 * Matches the expected format of model-service.py
 * 
 * Example:
 * {
 *   "answers": {
 *     "Q1": "نعم",
 *     "Q2": "لا",
 *     "Q3": "نعم"
 *   }
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModelRequest {
    /**
     * Map of question IDs (e.g., "Q1", "Q2") to answer values
     * Values should be in Arabic or as strings:
     * - "نعم" or "1" for YES/LIKE
     * - "لا" or "0" for NO/DISLIKE
     * - Or numeric scale values as strings
     */
    private Map<String, String> answers;
}