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
@RequestMapping("/api/activity-provider-requests")
@RequiredArgsConstructor
public class ActivityProviderController {

    private final ActivityProviderService providerService;

    // ----------------- Invite Provider -----------------
    @PostMapping("/invite/{exhibitionId}/{providerId}")
    @PreAuthorize("hasRole('ORG_OWNER')")
    public ResponseEntity<ActivityProviderRequest> inviteProvider(
            @PathVariable Long exhibitionId,
            @PathVariable Long providerId,
            @RequestBody String orgRequirements,
            @RequestParam LocalDateTime responseDeadline,
            @AuthenticationPrincipal UserDetails userDetails) {

        ActivityProviderRequest request = providerService.inviteProvider(
                exhibitionId, providerId, orgRequirements, userDetails.getUsername(), responseDeadline);
        return ResponseEntity.ok(request);
    }

    // ----------------- Review Proposal -----------------
    @PostMapping("/review/{requestId}")
    @PreAuthorize("hasRole('ORG_OWNER')")
    public ResponseEntity<ActivityProviderRequest> reviewProposal(
            @PathVariable Long requestId,
            @RequestParam boolean approve,
            @RequestParam(required = false) String comments,
            @AuthenticationPrincipal UserDetails userDetails) {

        ActivityProviderRequest request = providerService.reviewProviderProposal(
                requestId, approve, comments, userDetails.getUsername());
        return ResponseEntity.ok(request);
    }
    
    // ----------------- Cancel Request -----------------
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ACTIVITY_PROVIDER')")
    public ResponseEntity<ActivityProviderRequest> cancelRequest(
            @PathVariable Long id,
            @RequestParam String reason,
            @AuthenticationPrincipal UserDetails userDetails) {
            
        ActivityProviderRequest cancelled = providerService.cancelRequest(id, reason, userDetails.getUsername());
        return ResponseEntity.ok(cancelled);
    }
}
