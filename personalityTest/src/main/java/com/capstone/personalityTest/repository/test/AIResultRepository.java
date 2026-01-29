package com.capstone.personalityTest.repository.test;

import com.capstone.personalityTest.model.testm.AIResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for AIResult entity.
 * Provides database operations for AI analysis results.
 */
@Repository
public interface AIResultRepository extends JpaRepository<AIResult, Long> {

    /**
     * Find AI result by test attempt ID.
     * Since AIResult has one-to-one relationship with TestAttempt,
     * this will return at most one result.
     * 
     * @param testAttemptId ID of the test attempt
     * @return Optional containing AIResult if found, empty otherwise
     */
    Optional<AIResult> findByTestAttemptId(Long testAttemptId);

    /**
     * Check if AI result exists for a test attempt.
     * Useful to avoid duplicate AI analysis.
     * 
     * @param testAttemptId ID of the test attempt
     * @return true if AI result exists, false otherwise
     */
    boolean existsByTestAttemptId(Long testAttemptId);
}
