package com.capstone.personalityTest.service.test;

import com.capstone.personalityTest.dto.RequestDTO.test.CompleteAIRequest;
import com.capstone.personalityTest.dto.CompleteAIResponse;
import com.capstone.personalityTest.dto.RequestDTO.test.StudentInfoDTO;
import com.capstone.personalityTest.model.testm.AIResult;
import com.capstone.personalityTest.model.testm.MLResult;
import com.capstone.personalityTest.model.testm.TestAttempt.TestAttempt;
import com.capstone.personalityTest.repository.test.AIResultRepository;
import com.capstone.personalityTest.repository.test.MLResultRepository;
import com.capstone.personalityTest.repository.test.TestAttemptRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

/**
 * Service for integrating with Python AI service.
 * Handles communication between Spring Boot and Python AI for complete personality analysis.
 * 
 * Flow:
 * 1. Spring Boot calculates personality code (traditional or ML-based)
 * 2. This service sends code + student info to Python AI
 * 3. Python AI runs: RAG → Learning Path → Job Matching → Email
 * 4. This service receives results and saves to database
 * 
 * Priority for personality code:
 * 1. ML-predicted code (if available in MLResult)
 * 2. Traditional calculated code (from EvaluationResult)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AIIntegrationService {

    // Injected dependencies
    private final RestTemplate restTemplate;
    private final AIResultRepository aiResultRepo;
    private final TestAttemptRepository testAttemptRepo;
    private final MLResultRepository mlResultRepo;

    /**
     * URL of Python AI service
     * Configured in application.properties
     * Default: http://localhost:5000
     */
    @Value("${ai.service.url:http://localhost:5000}")
    private String aiServiceUrl;

    /**
     * Trigger complete AI analysis for a test attempt.
     * This method runs asynchronously to avoid blocking test finalization.
     * 
     * Process:
     * 1. Extract personality code and student info from test attempt
     * 2. Build request DTO
     * 3. Call Python AI service via REST API
     * 4. Save AI results to database
     * 
     * @param attempt The finalized test attempt with calculated results
     */
    @Async
    @Transactional
    public void triggerCompleteAIAnalysis(Long attemptId) {
        try {
            log.info("🤖 Triggering AI analysis for attempt: {}", attemptId);

            // Fetch attempt within transaction to ensure it's attached and metricScores can be loaded
            TestAttempt attempt = testAttemptRepo.findById(attemptId)
                    .orElseThrow(() -> new EntityNotFoundException("Test attempt not found: " + attemptId));

            // Check if AI result already exists (avoid duplicate analysis)
//            if (aiResultRepo.existsByTestAttemptId(attempt.getId())) {
//                log.warn("⚠️  AI result already exists for attempt: {}", attempt.getId());
//                return;
//            }

            // Extract personality code from evaluation result
            // Format: "R-I-A" (top 3 metrics)
            String personalityCode = extractPersonalityCode(attempt);
            log.info("   Personality Code: {}", personalityCode);

            // Build student info DTO
            StudentInfoDTO studentInfo = StudentInfoDTO.builder()
                    .name(attempt.getStudent().getName())
                    .email(attempt.getStudent().getEmail())
                    .gender(attempt.getStudent().getGender().toString())
                    .build();

            // Build complete AI request
            CompleteAIRequest request = CompleteAIRequest.builder()
                    .attemptId(attempt.getId())
                    .personalityCode(personalityCode)
                    .studentInfo(studentInfo)
                    .metricScores(attempt.getEvaluationResult().getMetricScores())
                    .build();

            // Prepare HTTP request
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<CompleteAIRequest> entity = new HttpEntity<>(request, headers);

            // Call Python AI service
            log.info("📡 Calling Python AI: {}/api/ai/complete-analysis", aiServiceUrl);
            ResponseEntity<CompleteAIResponse> response = restTemplate.postForEntity(
                    aiServiceUrl + "/api/ai/complete-analysis",
                    entity,
                    CompleteAIResponse.class
            );

            // Check response
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // Save AI results to database
                saveAIResults(attempt, response.getBody());
                log.info("✅ AI analysis completed successfully for attempt: {}", attempt.getId());
            } else {
                log.warn("⚠️  Python AI returned non-success status: {}", response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("❌ Error during AI analysis for attempt {}: {}", attemptId, e.getMessage(), e);
        }
    }

    /**
     * Save AI analysis results to database.
     * Creates a new AIResult entity linked to the test attempt.
     * 
     * @param attempt The test attempt
     * @param aiResponse Response from Python AI service
     */
    private void saveAIResults(TestAttempt attempt, CompleteAIResponse aiResponse) {
        log.info("💾 Saving AI results to database...");

        // Create AIResult entity
        AIResult aiResult = new AIResult();
        aiResult.setTestAttempt(attempt);
        aiResult.setPersonalityCode(aiResponse.getPersonalityCode());
        aiResult.setCareerRecommendations(aiResponse.getCareerRecommendations());
        aiResult.setLearningPath(aiResponse.getLearningPath());
        aiResult.setJobMatches(aiResponse.getJobMatches());
        aiResult.setEmailSent(aiResponse.isEmailSent());
        aiResult.setCreatedAt(LocalDateTime.now());

        // Save to database
        aiResultRepo.save(aiResult);

        log.info("✅ AI results saved with ID: {}", aiResult.getId());
        log.info("   Career Recommendations: {} chars", aiResponse.getCareerRecommendations().length());
        log.info("   Learning Path: {} chars", aiResponse.getLearningPath().length());
        log.info("   Email Sent: {}", aiResponse.isEmailSent());
    }

    /**
     * Extract personality code for AI analysis.
     * 
     * Priority:
     * 1. ML-predicted code (if MLResult exists) - More accurate, data-driven
     * 2. Traditional calculated code (from EvaluationResult) - Fallback
     * 
     * Note: ML model returns format like "ISE", but we need "I-S-E" format.
     * This method automatically formats the code with hyphens.
     * 
     * @param attempt Test attempt with evaluation result
     * @return Personality code in format "X-Y-Z" (e.g., "I-S-E")
     */
    private String extractPersonalityCode(TestAttempt attempt) {
        // Check if ML prediction exists
        return mlResultRepo.findByTestAttemptId(attempt.getId())
                .map(mlResult -> {
                    String rawCode = mlResult.getPredictedCode();
                    log.info("   Using ML-predicted code (raw): {}", rawCode);
                    
                    // Format the code: "ISE" -> "I-S-E"
                    String formattedCode = formatPersonalityCode(rawCode);
                    log.info("   Formatted ML code: {}", formattedCode);
                    
                    return formattedCode;
                })
                .orElseGet(() -> {
                    // Fallback to traditional calculation
                    log.info("   Using traditional calculated code (no ML prediction found)");
                    String first = attempt.getEvaluationResult().getFirstMetric();
                    String second = attempt.getEvaluationResult().getSecondMetric();
                    String third = attempt.getEvaluationResult().getThirdMetric();
                    return first + "-" + second + "-" + third;
                });
    }

    /**
     * Format personality code to ensure consistent hyphenated format.
     * 
     * Handles both formats:
     * - "ISE" (ML model format) -> "I-S-E"
     * - "I-S-E" (already formatted) -> "I-S-E"
     * 
     * @param code Raw personality code from ML model
     * @return Formatted code with hyphens (e.g., "I-S-E")
     */
    private String formatPersonalityCode(String code) {
        if (code == null || code.isEmpty()) {
            return code;
        }
        
        // If already has hyphens, return as is
        if (code.contains("-")) {
            return code;
        }
        
        // Convert "ISE" to "I-S-E"
        // Split into individual characters and join with hyphens
        return code.chars()
                .mapToObj(c -> String.valueOf((char) c))
                .reduce((a, b) -> a + "-" + b)
                .orElse(code);
    }

    /**
     * Get AI result for a test attempt.
     * Returns null if AI analysis hasn't completed yet.
     * 
     * @param attemptId ID of the test attempt
     * @return AIResult if exists, null otherwise
     */
    public AIResult getAIResultByAttemptId(Long attemptId) {
        return aiResultRepo.findByTestAttemptId(attemptId)
                .orElse(null);
    }
}
