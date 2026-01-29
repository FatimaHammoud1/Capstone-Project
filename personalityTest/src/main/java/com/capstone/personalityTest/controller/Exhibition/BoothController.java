package com.capstone.personalityTest.controller.Exhibition;

import com.capstone.personalityTest.dto.RequestDTO.Exhibition.BoothAllocationUpdateRequest;
import com.capstone.personalityTest.dto.ResponseDTO.Exhibition.BoothResponse;
import com.capstone.personalityTest.service.Exhibition.BoothService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exhibitions")
@RequiredArgsConstructor
public class BoothController {

    private final BoothService boothService;

    // ----------------- GET BOOTHS -----------------
    @GetMapping("/{exhibitionId}/booths")
    public ResponseEntity<List<BoothResponse>> getBooths(@PathVariable Long exhibitionId) {
        return ResponseEntity.ok(boothService.getBoothsByExhibition(exhibitionId));
    }

    @GetMapping("/booths/{boothId}")
    public ResponseEntity<BoothResponse> getBoothById(@PathVariable Long boothId) {
        return ResponseEntity.ok(boothService.getBoothById(boothId));
    }

    @GetMapping("/booths/university/{universityParticipationId}")
    @PreAuthorize("hasAnyRole('ORG_OWNER', 'DEVELOPER', 'UNIVERSITY_ADMIN')")
    public ResponseEntity<List<BoothResponse>> getBoothsByUniversityParticipationId(@PathVariable Long universityParticipationId) {
        return ResponseEntity.ok(boothService.getBoothsByUniversityParticipationId(universityParticipationId));
    }

    @GetMapping("/booths/activity-provider/{activityProviderRequestId}")
    @PreAuthorize("hasAnyRole('ORG_OWNER', 'DEVELOPER', 'ACTIVITY_PROVIDER')")
    public ResponseEntity<List<BoothResponse>> getBoothsByActivityProviderRequest(@PathVariable Long activityProviderRequestId) {
        return ResponseEntity.ok(boothService.getBoothsByActivityProviderRequest(activityProviderRequestId));
    }

    // ----------------- UPDATE BOOTHS (Allocations) -----------------
    @PostMapping("/{exhibitionId}/booth-allocation")
    @PreAuthorize("hasAnyRole('ORG_OWNER', 'DEVELOPER')")
    public ResponseEntity<Void> updateBoothAllocation(
            @PathVariable Long exhibitionId,
            @RequestBody BoothAllocationUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        boothService.updateBoothAllocation(exhibitionId, request, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }
}
