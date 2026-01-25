package com.capstone.personalityTest.service.Exhibition;

import com.capstone.personalityTest.model.Exhibition.Activity;
import com.capstone.personalityTest.repository.Exhibition.ActivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private final ActivityRepository activityRepository;

    public List<Activity> getAllActivities() {
        return activityRepository.findAll();
    }

    public List<Activity> getActivitiesByProviderId(Long providerId) {
        return activityRepository.findByProviderId(providerId);
    }
}
