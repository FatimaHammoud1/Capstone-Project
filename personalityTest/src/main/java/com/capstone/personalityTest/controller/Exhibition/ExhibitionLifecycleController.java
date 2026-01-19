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
    public ResponseEntity<com.capstone.personalityTest.dto.ResponseDTO.Exhibition.ExhibitionResponse> startExhibition(
            @PathVariable Long exhibitionId,
            @AuthenticationPrincipal UserDetails userDetails) {

        com.capstone.personalityTest.dto.ResponseDTO.Exhibition.ExhibitionResponse started = lifecycleService.startExhibition(exhibitionId, userDetails.getUsername());
        return ResponseEntity.ok(started);
    }
    
    // ----------------- COMPLETE EXHIBITION -----------------
    @PostMapping("/complete/{exhibitionId}")
    @PreAuthorize("hasAnyRole('ORG_OWNER', 'DEVELOPER')")
    public ResponseEntity<com.capstone.personalityTest.dto.ResponseDTO.Exhibition.ExhibitionResponse> completeExhibition(
            @PathVariable Long exhibitionId,
            @AuthenticationPrincipal UserDetails userDetails) {

        com.capstone.personalityTest.dto.ResponseDTO.Exhibition.ExhibitionResponse completed = lifecycleService.completeExhibition(exhibitionId, userDetails.getUsername());
        return ResponseEntity.ok(completed);
    }
}
