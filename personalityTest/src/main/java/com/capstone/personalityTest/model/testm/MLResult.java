package com.capstone.personalityTest.model.testm;

import com.capstone.personalityTest.model.testm.TestAttempt.TestAttempt;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity to store ML Model prediction results for a test attempt.
 * 
 * This is separate from EvaluationResult (traditional metric-based calculation).
 * When AI analysis is triggered, it can use the ML-predicted personality code
 * instead of the traditional calculated code.
 * 
 * Example:
 * - Traditional (EvaluationResult): "R-I-A" (calculated from metric scores)
 * - ML Prediction (MLResult): "R-A-I" (predicted by SVM model)
 */
@Entity
@Table(name = "ml_results")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MLResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * One-to-one relationship with TestAttempt
     * Each test attempt can have at most one ML prediction
     */
    @OneToOne
    @JoinColumn(name = "test_attempt_id", unique = true, nullable = false)
    private TestAttempt testAttempt;

    /**
     * Predicted personality code from ML model
     * Format: "X-Y-Z" where X, Y, Z are metric codes
     * Example: "R-I-A"
     */
    @Column(name = "predicted_code", nullable = false)
    private String predictedCode;

    /**
     * Timestamp when ML prediction was made
     */
    @Column(name = "predicted_at", nullable = false)
    private LocalDateTime predictedAt;

 


    /**
     * Extract the first metric from predicted code.
     * Handles both formats: "ISE" and "I-S-E"
     * Example: "R-I-A" → "R" or "RIA" → "R"
     */
    public String getFirstMetric() {
        if (predictedCode == null || predictedCode.isEmpty()) {
            return null;
        }
        
        // If has hyphens, split by hyphen
        if (predictedCode.contains("-")) {
            String[] parts = predictedCode.split("-");
            return parts.length > 0 ? parts[0] : null;
        }
        
        // If no hyphens (e.g., "ISE"), return first character
        return String.valueOf(predictedCode.charAt(0));
    }

    /**
     * Extract the second metric from predicted code.
     * Handles both formats: "ISE" and "I-S-E"
     * Example: "R-I-A" → "I" or "RIA" → "I"
     */
    public String getSecondMetric() {
        if (predictedCode == null || predictedCode.isEmpty()) {
            return null;
        }
        
        // If has hyphens, split by hyphen
        if (predictedCode.contains("-")) {
            String[] parts = predictedCode.split("-");
            return parts.length > 1 ? parts[1] : null;
        }
        
        // If no hyphens (e.g., "ISE"), return second character
        return predictedCode.length() > 1 ? String.valueOf(predictedCode.charAt(1)) : null;
    }

    /**
     * Extract the third metric from predicted code.
     * Handles both formats: "ISE" and "I-S-E"
     * Example: "R-I-A" → "A" or "RIA" → "A"
     */
    public String getThirdMetric() {
        if (predictedCode == null || predictedCode.isEmpty()) {
            return null;
        }
        
        // If has hyphens, split by hyphen
        if (predictedCode.contains("-")) {
            String[] parts = predictedCode.split("-");
            return parts.length > 2 ? parts[2] : null;
        }
        
        // If no hyphens (e.g., "ISE"), return third character
        return predictedCode.length() > 2 ? String.valueOf(predictedCode.charAt(2)) : null;
    }

    @Override
    public String toString() {
        return "MLResult{" +
                "id=" + id +
                ", predictedCode='" + predictedCode + '\'' +
                ", predictedAt=" + predictedAt+
                '}';
    }
}
