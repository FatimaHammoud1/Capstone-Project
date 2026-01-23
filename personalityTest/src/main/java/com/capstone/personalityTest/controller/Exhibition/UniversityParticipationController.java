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
import java.util.List;

import com.capstone.personalityTest.dto.ResponseDTO.Exhibition.UniversityParticipationResponse;
import com.capstone.personalityTest.dto.ResponseDTO.Exhibition.UniversityResponse;

@RestController
@RequestMapping("/api/universities")
@RequiredArgsConstructor
public class UniversityParticipationController {

    private final UniversityParticipationService participationService;

    // ----------------- INVITE UNIVERSITY -----------------
    @PostMapping("/invite/{exhibitionId}/{universityId}")
    @PreAuthorize("hasAnyRole('ORG_OWNER', 'DEVELOPER')")
    public ResponseEntity<UniversityParticipationResponse> inviteUniversity(
            @PathVariable Long exhibitionId,
            @PathVariable Long universityId,
            @RequestParam BigDecimal participationFee,
            @RequestParam(required = false) LocalDateTime responseDeadline,
            @AuthenticationPrincipal UserDetails userDetails) {

        UniversityParticipationResponse participation = participationService.inviteUniversity(
                exhibitionId, universityId, participationFee, responseDeadline, userDetails.getUsername()
        );
        return ResponseEntity.ok(participation);
    }

    // ----------------- UNIVERSITY REGISTER -----------------
    @PostMapping("/register/{participationId}")
    @PreAuthorize("hasAnyRole('UNIVERSITY_ADMIN', 'DEVELOPER')")
    public ResponseEntity<UniversityParticipationResponse> registerUniversity(
            @PathVariable Long participationId,
            @RequestBody Map<Long, Map<String, Object>> boothDetails, // boothId -> {content, contributors}
            @RequestParam int requestedBooths,
            @AuthenticationPrincipal UserDetails userDetails) {

        UniversityParticipationResponse registered = participationService.registerUniversity(
                participationId, requestedBooths, boothDetails, userDetails.getUsername()
        );
        return ResponseEntity.ok(registered);
    }

    // ----------------- APPROVE/REJECT UNIVERSITY -----------------
    @PostMapping("/review/{participationId}")
    @PreAuthorize("hasAnyRole('ORG_OWNER', 'DEVELOPER')")
    public ResponseEntity<UniversityParticipationResponse> reviewUniversity(
            @PathVariable Long participationId,
            @RequestParam boolean approve,
            @RequestParam(required = false) LocalDateTime confirmationDeadline,
            @AuthenticationPrincipal UserDetails userDetails) {

        UniversityParticipationResponse updated = participationService.reviewUniversity(
                participationId, approve, confirmationDeadline, userDetails.getUsername()
        );
        return ResponseEntity.ok(updated);
    }

    // ----------------- FINALIZE PARTICIPATION -----------------
    @PostMapping("/finalize/{participationId}")
    @PreAuthorize("hasAnyRole('UNIVERSITY_ADMIN', 'DEVELOPER')")
    public ResponseEntity<UniversityParticipationResponse> finalizeParticipation(
            @PathVariable Long participationId,
            @AuthenticationPrincipal UserDetails userDetails) {
            
        UniversityParticipationResponse finalized = participationService.finalizeParticipation(participationId, userDetails.getUsername());
        return ResponseEntity.ok(finalized);
    }
    
    // ----------------- CONFIRM PAYMENT -----------------
    @PostMapping("/confirm-payment/{participationId}")
    @PreAuthorize("hasAnyRole('ORG_OWNER', 'DEVELOPER')")
    public ResponseEntity<UniversityParticipationResponse> confirmPayment(
            @PathVariable Long participationId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        UniversityParticipationResponse confirmed = participationService.confirmPayment(participationId, userDetails.getUsername());
        return ResponseEntity.ok(confirmed);
    }
    
    // ----------------- CANCEL PARTICIPATION -----------------
    // ----------------- CANCEL PARTICIPATION -----------------
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('UNIVERSITY_ADMIN', 'ORG_OWNER', 'DEVELOPER')")
    public ResponseEntity<UniversityParticipationResponse> cancelParticipation(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
            
        UniversityParticipationResponse cancelled = participationService.cancelParticipation(id, userDetails.getUsername());
        return ResponseEntity.ok(cancelled);
    }

    // ----------------- GET PARTICIPATIONS -----------------
    @GetMapping("/{participationId}")
    @PreAuthorize("hasAnyRole('ORG_OWNER', 'UNIVERSITY_ADMIN', 'DEVELOPER')")
    public ResponseEntity<UniversityParticipationResponse> getParticipationById(@PathVariable Long participationId) {
        return ResponseEntity.ok(participationService.getParticipationById(participationId));
    }


    @GetMapping("/exhibition/{exhibitionId}")
    @PreAuthorize("hasAnyRole('ORG_OWNER', 'DEVELOPER')")
    public ResponseEntity<List<UniversityParticipationResponse>> getParticipationsByExhibition(@PathVariable Long exhibitionId) {
        return ResponseEntity.ok(participationService.getParticipationsByExhibition(exhibitionId));
    }

    @GetMapping("/university/{universityId}/participations")
    @PreAuthorize("hasAnyRole('UNIVERSITY_ADMIN', 'DEVELOPER')")
    public ResponseEntity<List<UniversityParticipationResponse>> getAllParticipationsByUniversityId(@PathVariable Long universityId) {
        return ResponseEntity.ok(participationService.getParticipationsByUniversityId(universityId));
    }
    
    // ----------------- Get All Active Universities -----------------
    @GetMapping("/all-universities")
    @PreAuthorize("hasAnyRole('ORG_OWNER', 'DEVELOPER')")
    public ResponseEntity<List<UniversityResponse>> getAllActiveUniversities() {
        return ResponseEntity.ok(participationService.getAllActiveUniversities());
    }

    // ----------------- Get Bank University By ID -----------------
    @GetMapping("/university/{universityId}")
    @PreAuthorize("hasAnyRole('ORG_OWNER', 'DEVELOPER')")
    public ResponseEntity<UniversityResponse> getUniversityById(@PathVariable Long universityId) {
        return ResponseEntity.ok(participationService.getUniversityById(universityId));
    }

    // ----------------- Get Universities By Owner ID -----------------
    @GetMapping("/owner/{ownerId}")
    @PreAuthorize("hasAnyRole('ORG_OWNER', 'DEVELOPER', 'UNIVERSITY_ADMIN')")
    public ResponseEntity<List<UniversityResponse>> getAllUniversitiesByOwnerId(@PathVariable Long ownerId) {
        return ResponseEntity.ok(participationService.getUniversitiesByOwnerId(ownerId));
    }
}
