package com.capstone.personalityTest.repository.Exhibition;

import com.capstone.personalityTest.model.Enum.Exhibition.ParticipationStatus;
import com.capstone.personalityTest.model.Exhibition.Exhibition;
import com.capstone.personalityTest.model.Exhibition.School;
import com.capstone.personalityTest.model.Exhibition.SchoolParticipation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SchoolParticipationRepository extends JpaRepository<SchoolParticipation, Long> {
    boolean existsByExhibitionAndSchool(Exhibition exhibition, School school);
    List<SchoolParticipation> findByExhibitionId(Long exhibitionId);
    boolean existsByExhibitionIdAndStatus(Long exhibitionId, ParticipationStatus status);
    List<SchoolParticipation> findByExhibitionAndStatus(Exhibition exhibition, ParticipationStatus status);
    List<SchoolParticipation> findBySchoolId(Long schoolId);
}
