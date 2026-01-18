package com.capstone.personalityTest.controller.Exhibition;

import com.capstone.personalityTest.model.Exhibition.ExhibitionFeedback;
import com.capstone.personalityTest.service.Exhibition.ExhibitionFeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/feedback")
@RequiredArgsConstructor
public class ExhibitionFeedbackController {

    private final ExhibitionFeedbackService feedbackService;

    // ----------------- Submit Feedback -----------------
    @PostMapping("/submit")
    @PreAuthorize("hasAnyRole('STUDENT', 'DEVELOPER')")
    public ResponseEntity<ExhibitionFeedback> submitFeedback(
            @RequestParam Long exhibitionId,
            @RequestParam Integer rating,
            @RequestParam(required = false) String comments,
            @AuthenticationPrincipal UserDetails userDetails) {

        // 7️⃣ Security fix: Do not accept studentId from request parameters.
        ExhibitionFeedback feedback = feedbackService.submitFeedback(exhibitionId, userDetails.getUsername(), rating, comments);
        return ResponseEntity.ok(feedback);
    }

    // ----------------- Get Feedback for Exhibition (ORG_OWNER) -----------------
    @GetMapping("/exhibition/{exhibitionId}")
    @PreAuthorize("hasAnyRole('ORG_OWNER', 'DEVELOPER')")
    public ResponseEntity<List<ExhibitionFeedback>> getFeedbackForExhibition(@PathVariable Long exhibitionId) {
        List<ExhibitionFeedback> feedbackList = feedbackService.getFeedbackForExhibition(exhibitionId);
        return ResponseEntity.ok(feedbackList);
    }
}
