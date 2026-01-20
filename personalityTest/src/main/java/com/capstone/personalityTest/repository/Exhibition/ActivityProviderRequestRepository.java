package com.capstone.personalityTest.repository.Exhibition;

import com.capstone.personalityTest.model.Exhibition.ActivityProviderRequest;
import com.capstone.personalityTest.model.Exhibition.Exhibition;
import com.capstone.personalityTest.model.Exhibition.ActivityProvider;
import com.capstone.personalityTest.model.Enum.Exhibition.ActivityProviderRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;

@Repository
public interface ActivityProviderRequestRepository extends JpaRepository<ActivityProviderRequest, Long> {
    boolean existsByExhibitionAndProvider(Exhibition exhibition, ActivityProvider provider);
    List<ActivityProviderRequest> findByExhibitionIdAndStatus(Long exhibitionId, ActivityProviderRequestStatus status);
    
    long countByExhibition(Exhibition exhibition);
    long countByExhibitionAndStatus(Exhibition exhibition, ActivityProviderRequestStatus status);
    
    boolean existsByExhibitionIdAndStatus(Long exhibitionId, ActivityProviderRequestStatus status);


    List<ActivityProviderRequest> findByExhibitionId(Long exhibitionId);
}
