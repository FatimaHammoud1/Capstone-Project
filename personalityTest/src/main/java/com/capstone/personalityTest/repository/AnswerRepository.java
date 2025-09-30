package com.capstone.personalityTest.repository;

import com.capstone.personalityTest.model.TestAttempt.Answer.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AnswerRepository extends JpaRepository<Answer, Long> {

    @Query("SELECT a FROM Answer a " +
            "WHERE a.testAttempt.id = :attemptId " +
            "AND a.question.id = :questionId " +
            "AND ((:subQuestionId IS NULL AND a.subQuestion IS NULL) " +
            "     OR (a.subQuestion.id = :subQuestionId))")
    Optional<Answer> findByAttemptAndQuestionAndSubQuestion(
            @Param("attemptId") Long attemptId,
            @Param("questionId") Long questionId,
            @Param("subQuestionId") Long subQuestionId);
}
