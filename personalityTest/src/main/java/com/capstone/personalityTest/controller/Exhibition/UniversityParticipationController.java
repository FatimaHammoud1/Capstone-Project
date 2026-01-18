package com.capstone.personalityTest.controller.Exhibition;

import com.capstone.personalityTest.model.Exhibition.UniversityParticipation;
import com.capstone.personalityTest.service.Exhibition.UniversityParticipationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/universities")
@RequiredArgsConstructor
public class UniversityParticipationController {

    private final UniversityParticipationService participationService;

    // ----------------- INVITE UNIVERSITY -----------------
    @PostMapping("/invite/{exhibitionId}/{universityId}")
    @PreAuthorize("hasRole('ORG_OWNER')")
    public ResponseEntity<UniversityParticipation> inviteUniversity(
            @PathVariable Long exhibitionId,
            @PathVariable Long universityId,
            @RequestParam BigDecimal participationFee,
            @RequestParam(required = false) LocalDateTime responseDeadline,
            @AuthenticationPrincipal UserDetails userDetails) {

        UniversityParticipation participation = participationService.inviteUniversity(
                exhibitionId, universityId, participationFee, responseDeadline, userDetails.getUsername()
        );
        return ResponseEntity.ok(participation);
    }

    // ----------------- UNIVERSITY REGISTER -----------------
    @PostMapping("/register/{participationId}")
    @PreAuthorize("hasRole('UNIVERSITY_ADMIN')")
    public ResponseEntity<UniversityParticipation> registerUniversity(
            @PathVariable Long participationId,
            @RequestBody Map<Long, Map<String, Object>> boothDetails, // boothId -> {content, contributors}
            @RequestParam int requestedBooths,
            @AuthenticationPrincipal UserDetails userDetails) {

        UniversityParticipation registered = participationService.registerUniversity(
                participationId, requestedBooths, boothDetails, userDetails.getUsername()
        );
        return ResponseEntity.ok(registered);
    }

    // ----------------- APPROVE/REJECT UNIVERSITY -----------------
    @PostMapping("/review/{participationId}")
    @PreAuthorize("hasRole('ORG_OWNER')")
    public ResponseEntity<UniversityParticipation> reviewUniversity(
            @PathVariable Long participationId,
            @RequestParam boolean approve,
            @AuthenticationPrincipal UserDetails userDetails) {

        UniversityParticipation updated = participationService.reviewUniversity(
                participationId, approve, userDetails.getUsername()
        );
        return ResponseEntity.ok(updated);
    }
}
