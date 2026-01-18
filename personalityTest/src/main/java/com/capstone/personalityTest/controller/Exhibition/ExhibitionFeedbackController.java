package com.capstone.personalityTest.controller.Exhibition;

import com.capstone.personalityTest.model.Exhibition.ExhibitionFeedback;
import com.capstone.personalityTest.service.Exhibition.ExhibitionFeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/feedback")
@RequiredArgsConstructor
public class ExhibitionFeedbackController {

    private final ExhibitionFeedbackService feedbackService;

    // ----------------- Submit Feedback -----------------
    @PostMapping("/submit")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ExhibitionFeedback> submitFeedback(
            @RequestParam Long exhibitionId,
            @RequestParam Long studentId,
            @RequestParam Integer rating,
            @RequestParam(required = false) String comments) {

        ExhibitionFeedback feedback = feedbackService.submitFeedback(exhibitionId, studentId, rating, comments);
        return ResponseEntity.ok(feedback);
    }

    // ----------------- Get Feedback for Exhibition (ORG_OWNER) -----------------
    @GetMapping("/exhibition/{exhibitionId}")
    @PreAuthorize("hasRole('ORG_OWNER')")
    public ResponseEntity<List<ExhibitionFeedback>> getFeedbackForExhibition(@PathVariable Long exhibitionId) {
        List<ExhibitionFeedback> feedbackList = feedbackService.getFeedbackForExhibition(exhibitionId);
        return ResponseEntity.ok(feedbackList);
    }
}
