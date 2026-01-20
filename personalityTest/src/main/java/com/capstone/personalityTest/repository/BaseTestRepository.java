package com.capstone.personalityTest.repository;

import com.capstone.personalityTest.model.Test.BaseTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BaseTestRepository extends JpaRepository<BaseTest, Long> {
    Optional<BaseTest> findByCode(String code);
}
