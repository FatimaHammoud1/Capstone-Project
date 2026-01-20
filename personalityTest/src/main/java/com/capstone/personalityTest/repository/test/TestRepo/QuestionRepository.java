package com.capstone.personalityTest.repository.test.TestRepo;

import com.capstone.personalityTest.model.testm.Test.Question;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepository extends JpaRepository<Question, Long> {
}
