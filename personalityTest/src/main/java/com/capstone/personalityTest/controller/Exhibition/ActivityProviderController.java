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
    @PreAuthorize("hasAnyRole('ORG_OWNER', 'DEVELOPER')")
    public ResponseEntity<com.capstone.personalityTest.dto.ResponseDTO.Exhibition.ActivityProviderRequestResponse> inviteProvider(
            @PathVariable Long exhibitionId,
            @PathVariable Long providerId,
            @RequestBody String orgRequirements,
            @RequestParam LocalDateTime responseDeadline,
            @AuthenticationPrincipal UserDetails userDetails) {

        com.capstone.personalityTest.dto.ResponseDTO.Exhibition.ActivityProviderRequestResponse request = providerService.inviteProvider(
                exhibitionId, providerId, orgRequirements, userDetails.getUsername(), responseDeadline);
        return ResponseEntity.ok(request);
    }

    // ----------------- Submit Proposal -----------------
    @PostMapping("/submit-proposal/{requestId}")
    @PreAuthorize("hasAnyRole('ACTIVITY_PROVIDER', 'DEVELOPER')")
    public ResponseEntity<com.capstone.personalityTest.dto.ResponseDTO.Exhibition.ActivityProviderRequestResponse> submitProposal(
            @PathVariable Long requestId,
            @RequestBody java.util.Map<String, Object> body,
            @AuthenticationPrincipal UserDetails userDetails) {

        String proposalText = (String) body.get("proposalText");
        Integer boothsCount = (Integer) body.get("boothsCount");
        // Handle BigDecimal conversion safely
        java.math.BigDecimal totalCost = new java.math.BigDecimal(body.get("totalCost").toString());

        com.capstone.personalityTest.dto.ResponseDTO.Exhibition.ActivityProviderRequestResponse response = providerService.submitProposal(
                requestId, proposalText, boothsCount, totalCost, userDetails.getUsername());
        
        return ResponseEntity.ok(response);
    }

    // ----------------- Review Proposal -----------------
    @PostMapping("/review/{requestId}")
    @PreAuthorize("hasAnyRole('ORG_OWNER', 'DEVELOPER')")
    public ResponseEntity<com.capstone.personalityTest.dto.ResponseDTO.Exhibition.ActivityProviderRequestResponse> reviewProposal(
            @PathVariable Long requestId,
            @RequestParam boolean approve,
            @RequestParam(required = false) String comments,
            @AuthenticationPrincipal UserDetails userDetails) {

        com.capstone.personalityTest.dto.ResponseDTO.Exhibition.ActivityProviderRequestResponse request = providerService.reviewProviderProposal(
                requestId, approve, comments, userDetails.getUsername());
        return ResponseEntity.ok(request);
    }
    
    // ----------------- Cancel Request -----------------
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ACTIVITY_PROVIDER', 'DEVELOPER')")
    public ResponseEntity<com.capstone.personalityTest.dto.ResponseDTO.Exhibition.ActivityProviderRequestResponse> cancelRequest(
            @PathVariable Long id,
            @RequestParam String reason,
            @AuthenticationPrincipal UserDetails userDetails) {
            
        com.capstone.personalityTest.dto.ResponseDTO.Exhibition.ActivityProviderRequestResponse cancelled = providerService.cancelRequest(id, reason, userDetails.getUsername());
        return ResponseEntity.ok(cancelled);
    }
}
