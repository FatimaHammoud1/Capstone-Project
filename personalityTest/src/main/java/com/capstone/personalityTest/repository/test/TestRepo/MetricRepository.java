package com.capstone.personalityTest.repository.test.TestRepo;

import com.capstone.personalityTest.model.testm.Test.Metric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MetricRepository extends JpaRepository<Metric, Long> {
    Optional<Metric> findByCode(String code);

    List<Metric> findByBaseTestId(Long baseTestId);
}
