package com.capstone.personalityTest.controller.Exhibition;

import com.capstone.personalityTest.model.Exhibition.Exhibition;
import com.capstone.personalityTest.service.Exhibition.ExhibitionLifecycleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/exhibitions")
@RequiredArgsConstructor
public class ExhibitionLifecycleController {

    private final ExhibitionLifecycleService lifecycleService;

    // ----------------- START EXHIBITION -----------------
    @PostMapping("/start/{exhibitionId}")
    @PreAuthorize("hasAnyRole('ORG_OWNER', 'DEVELOPER')")
    public ResponseEntity<Exhibition> startExhibition(
            @PathVariable Long exhibitionId,
            @AuthenticationPrincipal UserDetails userDetails) {

        Exhibition started = lifecycleService.startExhibition(exhibitionId, userDetails.getUsername());
        return ResponseEntity.ok(started);
    }
    
    // ----------------- COMPLETE EXHIBITION -----------------
    @PostMapping("/complete/{exhibitionId}")
    @PreAuthorize("hasAnyRole('ORG_OWNER', 'DEVELOPER')")
    public ResponseEntity<Exhibition> completeExhibition(
            @PathVariable Long exhibitionId,
            @AuthenticationPrincipal UserDetails userDetails) {

        Exhibition completed = lifecycleService.completeExhibition(exhibitionId, userDetails.getUsername());
        return ResponseEntity.ok(completed);
    }
}
