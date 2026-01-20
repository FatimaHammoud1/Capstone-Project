package com.capstone.personalityTest.controller.Exhibition;

import com.capstone.personalityTest.model.Exhibition.Venue;
import com.capstone.personalityTest.repository.Exhibition.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/venues")
@RequiredArgsConstructor
public class VenueController {

    private final VenueRepository venueRepository;

    // ----------------- GET VENUES BY MUNICIPALITY ID -----------------
    @GetMapping("/municipality/{municipalityId}")
    @PreAuthorize("hasAnyRole('ORG_OWNER', 'DEVELOPER')")
    public ResponseEntity<List<Venue>> getVenuesByMunicipality(@PathVariable Long municipalityId) {
        List<Venue> venues = venueRepository.findByMunicipalityId(municipalityId);
        return ResponseEntity.ok(venues);
    }

    // ----------------- GET VENUE BY ID -----------------
    @GetMapping("/{venueId}")
    @PreAuthorize("hasAnyRole('ORG_OWNER', 'DEVELOPER')")
    public ResponseEntity<Venue> getVenueById(@PathVariable Long venueId) {
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new RuntimeException("Venue not found"));
        return ResponseEntity.ok(venue);
    }
}
