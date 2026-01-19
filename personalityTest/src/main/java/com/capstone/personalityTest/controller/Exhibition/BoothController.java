package com.capstone.personalityTest.controller.Exhibition;

import com.capstone.personalityTest.dto.RequestDTO.Exhibition.ScheduleUpdateRequest;
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
    @PreAuthorize("hasAnyRole('ORG_OWNER', 'DEVELOPER')")
    public ResponseEntity<List<BoothResponse>> getBooths(@PathVariable Long exhibitionId) {
        return ResponseEntity.ok(boothService.getBoothsByExhibition(exhibitionId));
    }

    // ----------------- UPDATE BOOTHS (Schedule & Zone) -----------------
    @PostMapping("/{exhibitionId}/schedule")
    @PreAuthorize("hasAnyRole('ORG_OWNER', 'DEVELOPER')")
    public ResponseEntity<Void> updateScheduleAndBooths(
            @PathVariable Long exhibitionId,
            @RequestBody ScheduleUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        boothService.updateBoothAllocations(exhibitionId, request, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }
}
