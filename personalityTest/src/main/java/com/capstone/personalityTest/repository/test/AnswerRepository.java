package com.capstone.personalityTest.repository.test;

import com.capstone.personalityTest.model.testm.TestAttempt.Answer.Answer;
import com.capstone.personalityTest.model.testm.TestAttempt.TestAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AnswerRepository extends JpaRepository<Answer, Long> {

    List<Answer> findByTestAttempt(TestAttempt testAttempt);

    List<Answer> findByTestAttemptId(Long testAttemptId);

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
