package com.capstone.personalityTest.repository.Exhibition;

import com.capstone.personalityTest.model.Enum.Exhibition.BoothType;
import com.capstone.personalityTest.model.Exhibition.Booth;
import com.capstone.personalityTest.model.Exhibition.Exhibition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BoothRepository extends JpaRepository<Booth, Long> {
    
    int countByExhibition(Exhibition exhibition);

    void deleteByExhibition(Exhibition exhibition);

    @Query("""
        SELECT COALESCE(SUM(b.maxParticipants), 0)
        FROM Booth b
        WHERE b.exhibition.id = :exhibitionId
          AND b.boothType = :type
    """)
    int sumMaxParticipantsByExhibitionIdAndType(
            @Param("exhibitionId") Long exhibitionId,
            @Param("type") BoothType type
    );

    List<Booth> findByUniversityParticipationId(Long universityParticipationId);
    
    List<Booth> findByExhibitionId(Long exhibitionId);

    List<Booth> findByActivityProviderRequestId(Long activityProviderRequestId);

}
