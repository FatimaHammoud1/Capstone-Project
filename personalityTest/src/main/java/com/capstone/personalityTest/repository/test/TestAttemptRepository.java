package com.capstone.personalityTest.repository.test;

import com.capstone.personalityTest.dto.ResponseDTO.test.TestAttemptResponse.TestAttemptResponse;
import com.capstone.personalityTest.model.testm.TestAttempt.TestAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestAttemptRepository extends JpaRepository<TestAttempt, Long> {

    List<TestAttempt> findByStudentId(Long studentId);

    @Query("SELECT ta.test.baseTest.type, COUNT(ta) FROM TestAttempt ta GROUP BY ta.test.baseTest.type")
    List<Object[]> countAttemptsByBaseTestType();

    TestAttemptResponse getTestAttemptById(Long attemptId);



}
