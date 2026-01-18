package com.capstone.personalityTest.repository.Exhibition;

import com.capstone.personalityTest.model.Exhibition.Exhibition;
import com.capstone.personalityTest.model.Exhibition.University;
import com.capstone.personalityTest.model.Exhibition.UniversityParticipation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UniversityParticipationRepository extends JpaRepository<UniversityParticipation, Long> {
    boolean existsByExhibitionAndUniversity(Exhibition exhibition, University university);
}
