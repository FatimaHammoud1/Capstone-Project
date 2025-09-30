package com.capstone.personalityTest.repository;

import com.capstone.personalityTest.model.TestAttempt.TestAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TestAttemptRepository extends JpaRepository<TestAttempt, Long> {


}
