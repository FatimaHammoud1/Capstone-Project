package com.capstone.personalityTest.controller.Exhibition;

import com.capstone.personalityTest.model.Exhibition.Exhibition;
import com.capstone.personalityTest.service.Exhibition.ExhibitionLifecycleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import com.capstone.personalityTest.dto.ResponseDTO.Exhibition.ExhibitionResponse;

@RestController
@RequestMapping("/api/exhibitions")
@RequiredArgsConstructor
public class ExhibitionLifecycleController {

    private final ExhibitionLifecycleService lifecycleService;

    // ----------------- CONFIRM EXHIBITION -----------------
    @PostMapping("/confirm/{exhibitionId}")
    @PreAuthorize("hasAnyRole('ORG_OWNER', 'DEVELOPER')")
    public ResponseEntity<ExhibitionResponse> confirmExhibition(
            @PathVariable Long exhibitionId,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime finalizationDeadline,
            @AuthenticationPrincipal UserDetails userDetails) {

        ExhibitionResponse confirmed = lifecycleService.confirmExhibition(
                exhibitionId, finalizationDeadline, userDetails.getUsername()
        );
        return ResponseEntity.ok(confirmed);
    }

    // ----------------- START EXHIBITION -----------------
    @PostMapping("/start/{exhibitionId}")
    @PreAuthorize("hasAnyRole('ORG_OWNER', 'DEVELOPER')")
    public ResponseEntity<ExhibitionResponse> startExhibition(
            @PathVariable Long exhibitionId,
            @AuthenticationPrincipal UserDetails userDetails) {

        ExhibitionResponse started = lifecycleService.startExhibition(exhibitionId, userDetails.getUsername());
        return ResponseEntity.ok(started);
    }
    
    // ----------------- COMPLETE EXHIBITION -----------------
    @PostMapping("/complete/{exhibitionId}")
    @PreAuthorize("hasAnyRole('ORG_OWNER', 'MUNICIPALITY_ADMIN', 'DEVELOPER')")
    public ResponseEntity<ExhibitionResponse> completeExhibition(
            @PathVariable Long exhibitionId,
            @AuthenticationPrincipal UserDetails userDetails) {

        ExhibitionResponse completed = lifecycleService.completeExhibition(exhibitionId, userDetails.getUsername());
        return ResponseEntity.ok(completed);
    }
}
