package com.capstone.personalityTest.repository.test;

import com.capstone.personalityTest.model.testm.MLResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for MLResult entity.
 * Provides database operations for ML model prediction results.
 * 
 * This is separate from AIResultRepository which handles AI analysis results.
 * MLResult stores the personality code predicted by the ML model,
 * while AIResult stores the complete AI-generated career guidance.
 */
@Repository
public interface MLResultRepository extends JpaRepository<MLResult, Long> {

    /**
     * Find ML prediction result by test attempt ID.
     * Since MLResult has one-to-one relationship with TestAttempt,
     * this will return at most one result.
     * 
     * @param testAttemptId ID of the test attempt
     * @return Optional containing MLResult if found, empty otherwise
     */
    Optional<MLResult> findByTestAttemptId(Long testAttemptId);

    /**
     * Check if ML prediction exists for a test attempt.
     * Useful to avoid duplicate ML predictions.
     * 
     * @param testAttemptId ID of the test attempt
     * @return true if ML result exists, false otherwise
     */
    boolean existsByTestAttemptId(Long testAttemptId);

    /**
     * Delete ML result by test attempt ID.
     * Useful if you want to re-run ML prediction for a test attempt.
     * 
     * @param testAttemptId ID of the test attempt
     */
    void deleteByTestAttemptId(Long testAttemptId);
}
