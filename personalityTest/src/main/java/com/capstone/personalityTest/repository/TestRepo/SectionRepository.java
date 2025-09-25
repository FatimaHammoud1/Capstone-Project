package com.capstone.personalityTest.repository.TestRepo;

import com.capstone.personalityTest.model.Test.Section;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SectionRepository extends JpaRepository<Section, Long> {
}
