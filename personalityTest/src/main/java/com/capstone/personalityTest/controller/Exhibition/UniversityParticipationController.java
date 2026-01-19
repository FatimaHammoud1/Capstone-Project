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
    @PreAuthorize("hasAnyRole('ORG_OWNER', 'DEVELOPER')")
    public ResponseEntity<com.capstone.personalityTest.dto.ResponseDTO.Exhibition.UniversityParticipationResponse> inviteUniversity(
            @PathVariable Long exhibitionId,
            @PathVariable Long universityId,
            @RequestParam BigDecimal participationFee,
            @RequestParam(required = false) LocalDateTime responseDeadline,
            @AuthenticationPrincipal UserDetails userDetails) {

        com.capstone.personalityTest.dto.ResponseDTO.Exhibition.UniversityParticipationResponse participation = participationService.inviteUniversity(
                exhibitionId, universityId, participationFee, responseDeadline, userDetails.getUsername()
        );
        return ResponseEntity.ok(participation);
    }

    // ----------------- UNIVERSITY REGISTER -----------------
    @PostMapping("/register/{participationId}")
    @PreAuthorize("hasAnyRole('UNIVERSITY_ADMIN', 'DEVELOPER')")
    public ResponseEntity<com.capstone.personalityTest.dto.ResponseDTO.Exhibition.UniversityParticipationResponse> registerUniversity(
            @PathVariable Long participationId,
            @RequestBody Map<Long, Map<String, Object>> boothDetails, // boothId -> {content, contributors}
            @RequestParam int requestedBooths,
            @AuthenticationPrincipal UserDetails userDetails) {

        com.capstone.personalityTest.dto.ResponseDTO.Exhibition.UniversityParticipationResponse registered = participationService.registerUniversity(
                participationId, requestedBooths, boothDetails, userDetails.getUsername()
        );
        return ResponseEntity.ok(registered);
    }

    // ----------------- APPROVE/REJECT UNIVERSITY -----------------
    @PostMapping("/review/{participationId}")
    @PreAuthorize("hasAnyRole('ORG_OWNER', 'DEVELOPER')")
    public ResponseEntity<com.capstone.personalityTest.dto.ResponseDTO.Exhibition.UniversityParticipationResponse> reviewUniversity(
            @PathVariable Long participationId,
            @RequestParam boolean approve,
            @AuthenticationPrincipal UserDetails userDetails) {

        com.capstone.personalityTest.dto.ResponseDTO.Exhibition.UniversityParticipationResponse updated = participationService.reviewUniversity(
                participationId, approve, userDetails.getUsername()
        );
        return ResponseEntity.ok(updated);
    }

    // ----------------- FINALIZE PARTICIPATION -----------------
    @PostMapping("/finalize/{participationId}")
    @PreAuthorize("hasAnyRole('UNIVERSITY_ADMIN', 'DEVELOPER')")
    public ResponseEntity<com.capstone.personalityTest.dto.ResponseDTO.Exhibition.UniversityParticipationResponse> finalizeParticipation(
            @PathVariable Long participationId,
            @AuthenticationPrincipal UserDetails userDetails) {
            
        com.capstone.personalityTest.dto.ResponseDTO.Exhibition.UniversityParticipationResponse finalized = participationService.finalizeParticipation(participationId, userDetails.getUsername());
        return ResponseEntity.ok(finalized);
    }
    
    // ----------------- CONFIRM PAYMENT -----------------
    @PostMapping("/confirm-payment/{participationId}")
    @PreAuthorize("hasAnyRole('ORG_OWNER', 'DEVELOPER')")
    public ResponseEntity<com.capstone.personalityTest.dto.ResponseDTO.Exhibition.UniversityParticipationResponse> confirmPayment(
            @PathVariable Long participationId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        com.capstone.personalityTest.dto.ResponseDTO.Exhibition.UniversityParticipationResponse confirmed = participationService.confirmPayment(participationId, userDetails.getUsername());
        return ResponseEntity.ok(confirmed);
    }
    
    // ----------------- CANCEL PARTICIPATION -----------------
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('UNIVERSITY_ADMIN', 'ORG_OWNER', 'DEVELOPER')")
    public ResponseEntity<com.capstone.personalityTest.dto.ResponseDTO.Exhibition.UniversityParticipationResponse> cancelParticipation(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
            
        com.capstone.personalityTest.dto.ResponseDTO.Exhibition.UniversityParticipationResponse cancelled = participationService.cancelParticipation(id, userDetails.getUsername());
        return ResponseEntity.ok(cancelled);
    }
}
