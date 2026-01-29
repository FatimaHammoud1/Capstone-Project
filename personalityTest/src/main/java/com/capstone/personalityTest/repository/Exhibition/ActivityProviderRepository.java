package com.capstone.personalityTest.repository.Exhibition;

import com.capstone.personalityTest.model.Exhibition.ActivityProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActivityProviderRepository extends JpaRepository<ActivityProvider, Long> {
    java.util.List<ActivityProvider> findAllByOwnerId(Long ownerId);
}
