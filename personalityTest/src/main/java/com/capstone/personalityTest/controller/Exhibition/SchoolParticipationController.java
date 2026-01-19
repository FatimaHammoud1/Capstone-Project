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

import com.capstone.personalityTest.dto.ResponseDTO.Exhibition.SchoolParticipationResponse;

@RestController
@RequestMapping("/api/schools")
@RequiredArgsConstructor
public class SchoolParticipationController {

    private final SchoolParticipationService participationService;

    // ----------------- INVITE SCHOOL -----------------
    @PostMapping("/invite/{exhibitionId}/{schoolId}")
    @PreAuthorize("hasAnyRole('ORG_OWNER', 'DEVELOPER')")
    public ResponseEntity<SchoolParticipationResponse> inviteSchool(
            @PathVariable Long exhibitionId,
            @PathVariable Long schoolId,
            @RequestParam LocalDateTime responseDeadline,
            @AuthenticationPrincipal UserDetails userDetails) {

        SchoolParticipationResponse participation = participationService.inviteSchool(
                exhibitionId, schoolId, responseDeadline, userDetails.getUsername()
        );
        return ResponseEntity.ok(participation);
    }

    // ----------------- SCHOOL RESPOND (ACCEPT/REJECT) -----------------
    @PostMapping("/respond/{participationId}")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'DEVELOPER')")
    public ResponseEntity<SchoolParticipationResponse> respondToInvitation(
            @PathVariable Long participationId,
            @RequestParam boolean accept,
            @RequestParam(required = false) String rejectionReason,
            @RequestParam(required = false) Integer expectedStudents,
            @AuthenticationPrincipal UserDetails userDetails) {

        SchoolParticipationResponse participation = participationService.respondToInvitation(
                participationId, accept, rejectionReason, expectedStudents, userDetails.getUsername()
        );
        return ResponseEntity.ok(participation);
    }

    // ----------------- CONFIRM SCHOOL -----------------
    @PostMapping("/confirm/{participationId}")
    @PreAuthorize("hasAnyRole('ORG_OWNER', 'DEVELOPER')")
    public ResponseEntity<SchoolParticipationResponse> confirmParticipation(
            @PathVariable Long participationId,
            @RequestParam boolean approved,
            @AuthenticationPrincipal UserDetails userDetails) {

        SchoolParticipationResponse confirmed = participationService.confirmParticipation(participationId, approved, userDetails.getUsername());
        return ResponseEntity.ok(confirmed);
    }
    
    // ----------------- FINALIZE PARTICIPATION -----------------
    @PostMapping("/finalize/{participationId}")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'DEVELOPER')")
    public ResponseEntity<SchoolParticipationResponse> finalizeParticipation(
            @PathVariable Long participationId,
            @AuthenticationPrincipal UserDetails userDetails) {
            
        SchoolParticipationResponse finalized = participationService.finalizeParticipation(participationId, userDetails.getUsername());
        return ResponseEntity.ok(finalized);
    }
    
    // ----------------- CANCEL SCHOOL PARTICIPATION -----------------
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('SCHOOL_ADMIN', 'ORG_OWNER', 'DEVELOPER')")
    public ResponseEntity<SchoolParticipationResponse> cancelParticipation(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
            
        SchoolParticipationResponse cancelled = participationService.cancelParticipation(id, userDetails.getUsername());
        return ResponseEntity.ok(cancelled);
    }

    // ----------------- GET PARTICIPATIONS -----------------
    @GetMapping("/{participationId}")
    @PreAuthorize("hasAnyRole('ORG_OWNER', 'SCHOOL_ADMIN', 'DEVELOPER')")
    public ResponseEntity<SchoolParticipationResponse> getParticipationById(@PathVariable Long participationId) {
        return ResponseEntity.ok(participationService.getParticipationById(participationId));
    }

    @GetMapping("/exhibition/{exhibitionId}")
    @PreAuthorize("hasAnyRole('ORG_OWNER', 'DEVELOPER')")
    public ResponseEntity<java.util.List<SchoolParticipationResponse>> getParticipationsByExhibition(@PathVariable Long exhibitionId) {
        return ResponseEntity.ok(participationService.getParticipationsByExhibition(exhibitionId));
    }
}
