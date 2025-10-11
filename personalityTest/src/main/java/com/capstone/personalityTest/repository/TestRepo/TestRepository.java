package com.capstone.personalityTest.repository.TestRepo;

import com.capstone.personalityTest.model.Enum.TestStatus;
import com.capstone.personalityTest.model.Test.Test;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TestRepository extends JpaRepository<Test, Long> {
    List<Test> findByStatusAndActive(TestStatus status, boolean active);

}
