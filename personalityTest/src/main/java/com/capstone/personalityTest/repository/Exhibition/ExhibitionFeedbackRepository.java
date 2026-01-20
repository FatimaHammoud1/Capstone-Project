package com.capstone.personalityTest.repository.Exhibition;

import com.capstone.personalityTest.model.Exhibition.Exhibition;
import com.capstone.personalityTest.model.Exhibition.ExhibitionFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ExhibitionFeedbackRepository extends JpaRepository<ExhibitionFeedback, Long> {
    List<ExhibitionFeedback> findByExhibitionId(Long exhibitionId);
}
