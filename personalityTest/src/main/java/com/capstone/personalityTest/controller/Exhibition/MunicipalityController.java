package com.capstone.personalityTest.controller.Exhibition;

import com.capstone.personalityTest.model.Exhibition.VenueRequest;
import com.capstone.personalityTest.service.Exhibition.MunicipalityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/municipality")
@RequiredArgsConstructor
public class MunicipalityController {

    private final MunicipalityService municipalityService;

    // ----------------- REVIEW VENUE REQUEST -----------------
    @PostMapping("/review/{venueRequestId}")
    @PreAuthorize("hasRole('MUNICIPALITY_ADMIN')")
    public ResponseEntity<VenueRequest> reviewVenueRequest(
            @PathVariable Long venueRequestId,
            @RequestParam boolean approve,
            @RequestParam String responseText,
            @AuthenticationPrincipal UserDetails userDetails) {

        VenueRequest updatedRequest = municipalityService.reviewVenueRequest(
                venueRequestId, approve, responseText, userDetails.getUsername()
        );

        return ResponseEntity.ok(updatedRequest);
    }
}
