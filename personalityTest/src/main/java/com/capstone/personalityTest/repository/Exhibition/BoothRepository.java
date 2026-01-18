package com.capstone.personalityTest.repository.Exhibition;

import com.capstone.personalityTest.model.Exhibition.Booth;
import com.capstone.personalityTest.model.Exhibition.Exhibition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BoothRepository extends JpaRepository<Booth, Long> {
    
    int countByExhibition(Exhibition exhibition);

    void deleteByExhibition(Exhibition exhibition);
}
