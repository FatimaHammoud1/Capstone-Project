package com.capstone.personalityTest.repository.Exhibition;

import com.capstone.personalityTest.model.Enum.Exhibition.ParticipationStatus;
import com.capstone.personalityTest.model.Exhibition.Exhibition;
import com.capstone.personalityTest.model.Exhibition.University;
import com.capstone.personalityTest.model.Exhibition.UniversityParticipation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UniversityParticipationRepository extends JpaRepository<UniversityParticipation, Long> {
    boolean existsByExhibitionAndUniversity(Exhibition exhibition, University university);
    List<UniversityParticipation> findByExhibitionId(Long exhibitionId);
    boolean existsByExhibitionIdAndStatus(Long exhibitionId, ParticipationStatus status);
}
