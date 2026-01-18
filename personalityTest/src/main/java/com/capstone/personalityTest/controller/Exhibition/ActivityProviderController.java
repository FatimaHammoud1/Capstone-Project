package com.capstone.personalityTest.controller.Exhibition;

import com.capstone.personalityTest.model.Exhibition.ActivityProviderRequest;
import com.capstone.personalityTest.service.Exhibition.ActivityProviderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/activity-provider")
@RequiredArgsConstructor
public class ActivityProviderController {

    private final ActivityProviderService providerService;

    // ----------------- INVITE PROVIDER -----------------
    @PostMapping("/invite/{exhibitionId}/{providerId}")
    @PreAuthorize("hasRole('ORG_OWNER')")
    public ResponseEntity<ActivityProviderRequest> inviteProvider(
            @PathVariable Long exhibitionId,
            @PathVariable Long providerId,
            @RequestParam String orgRequirements,
            @RequestParam(required = false) LocalDateTime responseDeadline,
            @AuthenticationPrincipal UserDetails userDetails) {

        ActivityProviderRequest request = providerService.inviteProvider(
                exhibitionId, providerId, orgRequirements, userDetails.getUsername(), responseDeadline
        );

        return ResponseEntity.ok(request);
    }

    // ----------------- REVIEW PROVIDER PROPOSAL -----------------
    @PostMapping("/review/{requestId}")
    @PreAuthorize("hasRole('ORG_OWNER')")
    public ResponseEntity<ActivityProviderRequest> reviewProviderProposal(
            @PathVariable Long requestId,
            @RequestParam boolean approve,
            @RequestParam(required = false) String comments,
            @AuthenticationPrincipal UserDetails userDetails) {

        ActivityProviderRequest updated = providerService.reviewProviderProposal(
                requestId, approve, comments, userDetails.getUsername()
        );

        return ResponseEntity.ok(updated);
    }
}
