package com.capstone.personalityTest.controller.Exhibition;

import com.capstone.personalityTest.model.Exhibition.Activity;
import com.capstone.personalityTest.service.Exhibition.ActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/activities")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;

    // ----------------- List All Activities -----------------
    @GetMapping
    public ResponseEntity<List<Activity>> getAllActivities() {
        return ResponseEntity.ok(activityService.getAllActivities());
    }

    @GetMapping("/provider/{providerId}")
    public ResponseEntity<List<Activity>> getActivitiesByProviderId(@org.springframework.web.bind.annotation.PathVariable Long providerId) {
        return ResponseEntity.ok(activityService.getActivitiesByProviderId(providerId));
    }
}
