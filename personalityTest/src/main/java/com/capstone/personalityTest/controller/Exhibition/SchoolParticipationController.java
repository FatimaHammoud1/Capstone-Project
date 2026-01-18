package com.capstone.personalityTest.controller.Exhibition;

import com.capstone.personalityTest.model.Exhibition.SchoolParticipation;
import com.capstone.personalityTest.service.Exhibition.SchoolParticipationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/schools")
@RequiredArgsConstructor
public class SchoolParticipationController {

    private final SchoolParticipationService participationService;

    // ----------------- INVITE SCHOOL -----------------
    @PostMapping("/invite/{exhibitionId}/{schoolId}")
    @PreAuthorize("hasRole('ORG_OWNER')")
    public ResponseEntity<SchoolParticipation> inviteSchool(
            @PathVariable Long exhibitionId,
            @PathVariable Long schoolId,
            @RequestParam(required = false) LocalDateTime responseDeadline,
            @AuthenticationPrincipal UserDetails userDetails) {

        SchoolParticipation participation = participationService.inviteSchool(
                exhibitionId, schoolId, responseDeadline, userDetails.getUsername()
        );
        return ResponseEntity.ok(participation);
    }

    // ----------------- SCHOOL ACCEPT -----------------
    @PostMapping("/accept/{participationId}")
    @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    public ResponseEntity<SchoolParticipation> acceptInvitation(
            @PathVariable Long participationId,
            @AuthenticationPrincipal UserDetails userDetails) {

        SchoolParticipation accepted = participationService.acceptInvitation(
                participationId, userDetails.getUsername()
        );
        return ResponseEntity.ok(accepted);
    }

    // ----------------- CONFIRM SCHOOL -----------------
    @PostMapping("/confirm/{participationId}")
    @PreAuthorize("hasRole('ORG_OWNER')")
    public ResponseEntity<SchoolParticipation> confirmParticipation(
            @PathVariable Long participationId,
            @AuthenticationPrincipal UserDetails userDetails) {

        SchoolParticipation confirmed = participationService.confirmParticipation(
                participationId, userDetails.getUsername()
        );
        return ResponseEntity.ok(confirmed);
    }
}
