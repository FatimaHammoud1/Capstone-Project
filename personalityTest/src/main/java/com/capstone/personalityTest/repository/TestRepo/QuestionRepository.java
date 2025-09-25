package com.capstone.personalityTest.repository.TestRepo;

import com.capstone.personalityTest.model.Test.Question;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepository extends JpaRepository<Question, Long> {
}
