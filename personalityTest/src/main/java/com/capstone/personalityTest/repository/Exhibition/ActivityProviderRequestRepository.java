package com.capstone.personalityTest.repository.Exhibition;

import com.capstone.personalityTest.model.Exhibition.ActivityProviderRequest;
import com.capstone.personalityTest.model.Exhibition.Exhibition;
import com.capstone.personalityTest.model.Exhibition.ActivityProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActivityProviderRequestRepository extends JpaRepository<ActivityProviderRequest, Long> {
    boolean existsByExhibitionAndProvider(Exhibition exhibition, ActivityProvider provider);
}
