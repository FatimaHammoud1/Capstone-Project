package com.capstone.personalityTest.service.test;

import com.capstone.personalityTest.dto.ModelRequest;
import com.capstone.personalityTest.dto.ModelResponse;
import com.capstone.personalityTest.model.testm.TestAttempt.Answer.Answer;
import com.capstone.personalityTest.model.testm.TestAttempt.Answer.CheckBoxAnswer;
import com.capstone.personalityTest.model.testm.TestAttempt.Answer.ScaleAnswer;
import com.capstone.personalityTest.model.testm.TestAttempt.TestAttempt;
import com.capstone.personalityTest.repository.test.AnswerRepository;
import com.capstone.personalityTest.repository.test.TestAttemptRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for integrating with Python ML Model Service
 * Handles communication between Spring Boot and model-service.py
 * 
 * Flow:
 * 1. Retrieve all answers from a test attempt
 * 2. Transform them to model's expected format (Map<String, String>)
 * 3. Send to model service for personality code prediction
 * 4. Return predicted code
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ModelServiceClient {

    private final RestTemplate restTemplate;
    private final TestAttemptRepository testAttemptRepository;
    private final AnswerRepository answerRepository;

    /**
     * URL of Python ML Model Service
     * Configured in application.properties
     * Default: http://localhost:5001
     */
    @Value("${model.service.url:http://localhost:5001}")
    private String modelServiceUrl;

    /**
     * Get personality code from ML model for a test attempt
     * 
     * Process:
     * 1. Retrieve test attempt and its answers
     * 2. Transform answers to model format
     * 3. Call model-service.py prediction endpoint
     * 4. Extract and return predicted code
     * 
     * @param attemptId ID of the test attempt
     * @return Predicted personality code (e.g., "R-I-A")
     * @throws IllegalStateException if test attempt has no answers
     */
    public String getPredictedPersonalityCode(Long attemptId) {
        try {
            log.info("🤖 Getting personality code from ML Model for attempt: {}", attemptId);

            // Fetch test attempt
            TestAttempt attempt = testAttemptRepository.findById(attemptId)
                    .orElseThrow(() -> new EntityNotFoundException("Test attempt not found: " + attemptId));

            // Get all answers for this attempt
            List<Answer> answers = answerRepository.findByTestAttempt(attempt);
            
            if (answers.isEmpty()) {
                throw new IllegalStateException("No answers found for test attempt: " + attemptId);
            }

            log.info("   Found {} answers", answers.size());

            // Transform answers to model format
            Map<String, String> answersMap = transformAnswersToModelFormat(answers);
            log.info("   Transformed to model format with keys: {}", answersMap.keySet());

            // Build request
            ModelRequest request = ModelRequest.builder()
                    .answers(answersMap)
                    .build();

            // Prepare HTTP request
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<ModelRequest> entity = new HttpEntity<>(request, headers);

            // Call model service
            log.info("📡 Calling Model Service: {}/api/ml/predict-code", modelServiceUrl);
            ResponseEntity<ModelResponse> response = restTemplate.postForEntity(
                    modelServiceUrl + "/api/ml/predict-code",
                    entity,
                    ModelResponse.class
            );

            // Check response
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String predictedCode = response.getBody().getPredictedCode();
                log.info("✅ Predicted personality code: {}", predictedCode);
                return predictedCode;
            } else {
                log.warn("⚠️  Model Service returned non-success status: {}", response.getStatusCode());
                throw new RuntimeException("Model service returned error: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("❌ Error getting personality code from model: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get personality code from model service", e);
        }
    }

    /**
     * Transform Answer entities to model's expected format
     * 
     * Converts database answer objects to Map<String, String> format:
     * {
     *   "Q1": "نعم",
     *   "Q2": "لا",
     *   "Q3": "5"
     * }
     * 
     * Mapping:
     * - ScaleAnswer: Convert scaleValue (1-7) to string
     * - CheckBoxAnswer: Convert binaryValue to "نعم" (yes) or "لا" (no)
     * - OpenAnswer: Join multiple values with comma or take first value
     * 
     * Question ID format: "Q{index}" (Q1, Q2, Q3, etc.)
     * 
     * @param answers List of Answer entities from database
     * @return Map of question IDs to answer values in model format
     */
    private Map<String, String> transformAnswersToModelFormat(List<Answer> answers) {
        Map<String, String> answersMap = new HashMap<>();

        for (int i = 0; i < answers.size(); i++) {
            Answer answer = answers.get(i);
            String questionKey = "Q" + (i + 1);  // Q1, Q2, Q3, etc.
            
            String answerValue;

            if (answer instanceof ScaleAnswer) {
                // Scale answer: convert integer (1-7) to string
                ScaleAnswer scaleAnswer = (ScaleAnswer) answer;
                answerValue = String.valueOf(scaleAnswer.getScaleValue());
                log.debug("   {} (SCALE): {}", questionKey, answerValue);

            } else if (answer instanceof CheckBoxAnswer) {
                // Binary answer: convert boolean to Arabic yes/no
                CheckBoxAnswer checkBoxAnswer = (CheckBoxAnswer) answer;
                answerValue = checkBoxAnswer.getBinaryValue() ? "نعم" : "لا";
                log.debug("   {} (BINARY): {}", questionKey, answerValue);
            }
            //  else if (answer instanceof OpenAnswer) {
            //     // Open answer: take first value or join multiple
            //     OpenAnswer openAnswer = (OpenAnswer) answer;
            //     if (openAnswer.getValues() != null && !openAnswer.getValues().isEmpty()) {
            //         answerValue = String.join(",", openAnswer.getValues());
            //     } else {
            //         answerValue = "";
            //     }
            //     log.debug("   {} (OPEN): {}", questionKey, answerValue);
        // }
             else {
                // Default: empty string
                answerValue = "";
                log.debug("   {} (UNKNOWN): empty", questionKey);
            }

            answersMap.put(questionKey, answerValue);
        }

        return answersMap;
    }
}