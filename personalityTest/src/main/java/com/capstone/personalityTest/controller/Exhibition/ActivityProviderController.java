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

import com.capstone.personalityTest.dto.ResponseDTO.Exhibition.ActivityProviderResponse;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.math.BigDecimal;
import com.capstone.personalityTest.dto.ResponseDTO.Exhibition.ActivityProviderRequestResponse;

@RestController
@RequestMapping("/api/activity-providers")
@RequiredArgsConstructor
public class ActivityProviderController {

    private final ActivityProviderService providerService;

    // ----------------- Invite Provider -----------------
    @PostMapping("/invite/{exhibitionId}/{providerId}")
    @PreAuthorize("hasAnyRole('ORG_OWNER', 'DEVELOPER')")
    public ResponseEntity<ActivityProviderRequestResponse> inviteProvider(
            @PathVariable Long exhibitionId,
            @PathVariable Long providerId,
            @RequestBody String orgRequirements,
            @RequestParam LocalDateTime responseDeadline,
            @AuthenticationPrincipal UserDetails userDetails) {

        ActivityProviderRequestResponse request = providerService.inviteProvider(
                exhibitionId, providerId, orgRequirements, userDetails.getUsername(), responseDeadline);
        return ResponseEntity.ok(request);
    }


    // ----------------- Submit Proposal -----------------
    @PostMapping("/submit-proposal/{requestId}")
    @PreAuthorize("hasAnyRole('ACTIVITY_PROVIDER', 'DEVELOPER')")
    public ResponseEntity<ActivityProviderRequestResponse> submitProposal(
            @PathVariable Long requestId,
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal UserDetails userDetails) {

        String proposalText = (String) body.get("proposalText");
        Integer boothsCount = (Integer) body.get("boothsCount");
        // Handle BigDecimal conversion safely
        BigDecimal totalCost = new BigDecimal(body.get("totalCost").toString());
        
        // Parse activity IDs
        List<Long> activityIds = null;
        if (body.containsKey("activityIds")) {
            // Need gentle casting
            List<?> rawList = (List<?>) body.get("activityIds");
            activityIds = new ArrayList<>();
            for (Object obj : rawList) {
                if (obj instanceof Integer) {
                    activityIds.add(((Integer) obj).longValue());
                } else if (obj instanceof Long) {
                    activityIds.add((Long) obj);
                } else if (obj instanceof String) {
                    activityIds.add(Long.parseLong((String) obj));
                }
            }
        }

        ActivityProviderRequestResponse response = providerService.submitProposal(
                requestId, proposalText, boothsCount, totalCost, activityIds, userDetails.getUsername());
        
        return ResponseEntity.ok(response);
    }

    // ----------------- Review Proposal -----------------
    @PostMapping("/review/{requestId}")
    @PreAuthorize("hasAnyRole('ORG_OWNER', 'DEVELOPER')")
    public ResponseEntity<ActivityProviderRequestResponse> reviewProposal(
            @PathVariable Long requestId,
            @RequestParam boolean approve,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime confirmationDeadline,
            @RequestParam(required = false) String comments,
            @AuthenticationPrincipal UserDetails userDetails) {

        ActivityProviderRequestResponse request = providerService.reviewProviderProposal(
                requestId, approve, confirmationDeadline, comments, userDetails.getUsername());
        return ResponseEntity.ok(request);
    }
    
    // ----------------- Confirm Participation -----------------
//    @PostMapping("/confirm/{requestId}")
//    @PreAuthorize("hasAnyRole('ACTIVITY_PROVIDER', 'DEVELOPER')")
//    public ResponseEntity<ActivityProviderRequestResponse> confirmProvider(
//            @PathVariable Long requestId,
//            @AuthenticationPrincipal UserDetails userDetails) {
//
//        ActivityProviderRequestResponse confirmed = providerService.confirmProvider(requestId, userDetails.getUsername());
//        return ResponseEntity.ok(confirmed);
//    }
    
    // ----------------- Finalize Participation -----------------
    @PostMapping("/finalize/{requestId}")
    @PreAuthorize("hasAnyRole('ACTIVITY_PROVIDER', 'DEVELOPER')")
    public ResponseEntity<ActivityProviderRequestResponse> finalizeParticipation(
            @PathVariable Long requestId,
            @AuthenticationPrincipal UserDetails userDetails) {
            
        ActivityProviderRequestResponse finalized = providerService.finalizeParticipation(requestId, userDetails.getUsername());
        return ResponseEntity.ok(finalized);
    }
    
    // ----------------- Cancel Request -----------------
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ACTIVITY_PROVIDER', 'DEVELOPER')")
    public ResponseEntity<ActivityProviderRequestResponse> cancelRequest(
            @PathVariable Long id,
            @RequestParam String reason,
            @AuthenticationPrincipal UserDetails userDetails) {
            
        ActivityProviderRequestResponse cancelled = providerService.cancelRequest(id, reason, userDetails.getUsername());
        return ResponseEntity.ok(cancelled);
    }

    // ----------------- Get Requests -----------------
    @GetMapping("/{requestId}")
    @PreAuthorize("hasAnyRole('ORG_OWNER', 'ACTIVITY_PROVIDER', 'DEVELOPER')")
    public ResponseEntity<ActivityProviderRequestResponse> getRequestById(
            @PathVariable Long requestId,
            @AuthenticationPrincipal UserDetails userDetails) {
        // Can add finer logic to ensure provider can only see their own request if not org owner
        return ResponseEntity.ok(providerService.getRequestById(requestId));
    }

    @GetMapping("/exhibition/{exhibitionId}")
    @PreAuthorize("hasAnyRole('ORG_OWNER', 'DEVELOPER')")
    public ResponseEntity<List<ActivityProviderRequestResponse>> getRequestsByExhibition(
            @PathVariable Long exhibitionId) {
        return ResponseEntity.ok(providerService.getRequestsByExhibition(exhibitionId));
    }

    @GetMapping("/provider/{providerId}/requests")
    @PreAuthorize("hasAnyRole('ACTIVITY_PROVIDER', 'DEVELOPER', 'ORG_OWNER')")
    public ResponseEntity<List<ActivityProviderRequestResponse>> getRequestsByProvider(
            @PathVariable Long providerId) {
        return ResponseEntity.ok(providerService.getRequestsByProvider(providerId));
    }
    
    // ----------------- Get All Active Providers -----------------
    @GetMapping("/all-providers")
    @PreAuthorize("hasAnyRole('ORG_OWNER', 'DEVELOPER')")
    public ResponseEntity<List<ActivityProviderResponse>> getAllActiveProviders() {
        return ResponseEntity.ok(providerService.getAllActiveProviders());
    }

    // ----------------- Get Provider By ID -----------------
    @GetMapping("/provider/{providerId}")
    @PreAuthorize("hasAnyRole('ORG_OWNER', 'DEVELOPER')")
    public ResponseEntity<ActivityProviderResponse> getProviderById(@PathVariable Long providerId) {
        return ResponseEntity.ok(providerService.getProviderById(providerId));
    }

    // ----------------- Get Activity Providers By Owner ID -----------------
    @GetMapping("/owner/{ownerId}")
    @PreAuthorize("hasAnyRole('ORG_OWNER', 'DEVELOPER', 'ACTIVITY_PROVIDER')")
    public ResponseEntity<List<ActivityProviderResponse>> getAllActivityProvidersByOwnerId(@PathVariable Long ownerId) {
        return ResponseEntity.ok(providerService.getActivityProvidersByOwnerId(ownerId));
    }
}
