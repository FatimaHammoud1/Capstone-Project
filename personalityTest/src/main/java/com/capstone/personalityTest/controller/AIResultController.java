package com.capstone.personalityTest.controller;

import com.capstone.personalityTest.dto.ResponseDTO.AIResultResponseDTO;
import com.capstone.personalityTest.mapper.AIResultMapper;
import com.capstone.personalityTest.model.AIResult;
import com.capstone.personalityTest.service.AIIntegrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for retrieving AI analysis results.
 * Provides endpoints for frontend to fetch AI-generated career guidance.
 * 
 * Endpoints:
 * - GET /api/ai-results/attempt/{attemptId} - Get AI results for a test attempt
 */
@RestController
@RequestMapping("/api/ai-results")
@RequiredArgsConstructor
public class AIResultController {

    private final AIIntegrationService aiService;
    private final AIResultMapper aiResultMapper;

    /**
     * Get AI analysis results for a test attempt.
     * 
     * Flow:
     * 1. Student completes test
     * 2. Spring Boot calculates code and triggers AI (async)
     * 3. Frontend polls this endpoint to check if AI results are ready
     * 4. Returns results when available, or 202 PROCESSING if still running
     * 
     * Response Codes:
     * - 200 OK: AI results available (includes DTO object)
     * - 202 PROCESSING: AI analysis still in progress (body is null)
     * - 404 NOT FOUND: Test attempt doesn't exist
     * 
     * @param attemptId ID of the test attempt
     * @return AIResultResponseDTO if available, null with 202 status if still processing
     */
    @GetMapping("/attempt/{attemptId}")
    public ResponseEntity<AIResultResponseDTO> getAIResultByAttempt(@PathVariable Long attemptId) {
        // Fetch AI result from database
        AIResult result = aiService.getAIResultByAttemptId(attemptId);

        // Check if result exists
        if (result == null) {
            // AI analysis not completed yet or failed
            // Return 202 PROCESSING to indicate client should retry later
            return ResponseEntity.status(HttpStatus.PROCESSING)
                    .body(null);
        }

        // AI results available - return mapped DTO
        return ResponseEntity.ok(aiResultMapper.toDto(result));
    }

    /**
     * Example response when AI results are ready:
     * {
     *   "id": 1,
     *   "personalityCode": "R-I-A",
     *   "careerRecommendations": "الشخصية الواقعية (R): تميل للمهن العملية...",
     *   "learningPath": "الجامعات الموصى بها: ...",
     *   "jobMatches": "[{\"title\":\"Software Engineer\",\"score\":85}...]",
     *   "emailSent": true,
     *   "createdAt": "2026-01-06T15:30:00"
     * }
     * 
     * Frontend usage:
     * 1. After test finalization, poll this endpoint every 2-3 seconds
     * 2. If status 202, show "Generating recommendations..." message
     * 3. If status 200, display the AI results to student
     */
}
