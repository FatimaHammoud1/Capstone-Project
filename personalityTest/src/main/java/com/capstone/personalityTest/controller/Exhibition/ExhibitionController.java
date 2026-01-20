package com.capstone.personalityTest.controller.Exhibition;

import com.capstone.personalityTest.model.Exhibition.Exhibition;
import com.capstone.personalityTest.service.Exhibition.ExhibitionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import com.capstone.personalityTest.dto.ResponseDTO.Exhibition.ExhibitionResponse;
import com.capstone.personalityTest.dto.RequestDTO.Exhibition.ExhibitionRequest;
import com.capstone.personalityTest.dto.ResponseDTO.Exhibition.InvitationCapacityResponse;
import com.capstone.personalityTest.dto.RequestDTO.Exhibition.BoothLimitsRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/exhibitions")
@RequiredArgsConstructor
public class ExhibitionController {

    private final ExhibitionService exhibitionService;

    // ----------------- Create Exhibition -----------------
    @PostMapping
    @PreAuthorize("hasAnyRole('ORG_OWNER', 'DEVELOPER')")
    public ResponseEntity<ExhibitionResponse> createExhibition(
            @RequestParam Long orgId,
            @RequestBody ExhibitionRequest exhibitionRequest,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(exhibitionService.createExhibition(orgId, exhibitionRequest, userDetails.getUsername()));
    }

    // ----------------- Get Exhibitions by Organization -----------------
    @GetMapping("/organization/{orgId}")
    @PreAuthorize("hasAnyRole('ORG_OWNER', 'DEVELOPER')")
    public ResponseEntity<List<ExhibitionResponse>> getExhibitionsByOrg(@PathVariable Long orgId) {
        return ResponseEntity.ok(exhibitionService.getExhibitionsByOrg(orgId));
    }
    
    // ----------------- Get Exhibition By ID -----------------
    @GetMapping("/{exhibitionId}")
    public ResponseEntity<ExhibitionResponse> getExhibition(@PathVariable Long exhibitionId) {
        // Publicly accessible for students/participants to view details before acting?
        // Or restricted? Assuming public or authenticated. 
        // If security needed, add PreAuthorize("isAuthenticated()")
        return ResponseEntity.ok(exhibitionService.getExhibitionById(exhibitionId));
    }

    // ----------------- Get All Active Exhibitions -----------------
    @GetMapping("/active")
    public ResponseEntity<List<ExhibitionResponse>> getActiveExhibitions() {
        // Publicly accessible list for students to browse
        return ResponseEntity.ok(exhibitionService.getAllActiveExhibitions());
    }
    

    // ----------------- Get Available Booths -----------------
    @GetMapping("/{exhibitionId}/available-booths")
    @PreAuthorize("hasAnyRole('ORG_OWNER', 'DEVELOPER')")
    public ResponseEntity<Map<String, Integer>> getAvailableBooths(@PathVariable Long exhibitionId) {
        return ResponseEntity.ok(exhibitionService.getAvailableBooths(exhibitionId));
    }
    
    // ----------------- Set Booth Limits -----------------
    @PostMapping("/{exhibitionId}/booth-limits")
    @PreAuthorize("hasAnyRole('ORG_OWNER', 'DEVELOPER')")
    public ResponseEntity<InvitationCapacityResponse> setBoothLimits(
            @PathVariable Long exhibitionId,
            @RequestBody BoothLimitsRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        return ResponseEntity.ok(exhibitionService.setBoothLimits(exhibitionId, request, userDetails.getUsername()));
    }
    
    // ----------------- Cancel Exhibition -----------------
    @PostMapping("/{exhibitionId}/cancel")
    @PreAuthorize("hasAnyRole('ORG_OWNER', 'MUNICIPALITY_ADMIN', 'DEVELOPER')")
    public ResponseEntity<Exhibition> cancelExhibition(
            @PathVariable Long exhibitionId,
            @RequestParam String reason,
            @AuthenticationPrincipal UserDetails userDetails) {
            
        Exhibition cancelled = exhibitionService.cancelExhibition(exhibitionId, reason, userDetails.getUsername());
        return ResponseEntity.ok(cancelled);
    }
}
