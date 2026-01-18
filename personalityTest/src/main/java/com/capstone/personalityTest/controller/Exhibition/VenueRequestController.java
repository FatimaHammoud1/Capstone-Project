package com.capstone.personalityTest.controller.Exhibition;

import com.capstone.personalityTest.model.Exhibition.VenueRequest;
import com.capstone.personalityTest.service.Exhibition.VenueRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/venue-requests")
@RequiredArgsConstructor
public class VenueRequestController {

    private final VenueRequestService venueRequestService;

    // ----------------- CREATE VENUE REQUEST -----------------
    @PostMapping("/create/{exhibitionId}/{venueId}")
    @PreAuthorize("hasRole('ORG_OWNER')")
    public ResponseEntity<VenueRequest> createVenueRequest(
            @PathVariable Long exhibitionId,
            @PathVariable Long venueId,
            @RequestBody Map<String, String> body,
            @RequestParam("responseDeadline") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime responseDeadline,
            @AuthenticationPrincipal UserDetails userDetails) {

        String orgNotes = body.get("orgNotes");

        VenueRequest request = venueRequestService.createVenueRequest(
                exhibitionId, venueId, orgNotes, responseDeadline, userDetails.getUsername()
        );

        return ResponseEntity.ok(request);
    }

    // ----------------- GET ALL REQUESTS FOR EXHIBITION -----------------
    @GetMapping("/exhibition/{exhibitionId}")
    @PreAuthorize("hasRole('ORG_OWNER')")
    public ResponseEntity<List<VenueRequest>> getRequests(@PathVariable Long exhibitionId) {
        return ResponseEntity.ok(venueRequestService.getRequestsForExhibition(exhibitionId));
    }
}
