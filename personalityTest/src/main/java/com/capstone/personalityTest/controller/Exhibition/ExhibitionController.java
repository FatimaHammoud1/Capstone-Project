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

@RestController
@RequestMapping("/api/exhibitions")
@RequiredArgsConstructor
public class ExhibitionController {

    private final ExhibitionService exhibitionService;

    // ----------------- Create Exhibition -----------------
    @PostMapping
    @PreAuthorize("hasAnyRole('ORG_OWNER', 'DEVELOPER')")
    public ResponseEntity<com.capstone.personalityTest.dto.ResponseDTO.Exhibition.ExhibitionResponse> createExhibition(
            @RequestParam Long orgId,
            @RequestBody com.capstone.personalityTest.dto.RequestDTO.Exhibition.ExhibitionRequest exhibitionRequest,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(exhibitionService.createExhibition(orgId, exhibitionRequest, userDetails.getUsername()));
    }

    // ----------------- Get Exhibitions by Organization -----------------
    @GetMapping("/organization/{orgId}")
    @PreAuthorize("hasAnyRole('ORG_OWNER', 'DEVELOPER')")
    public ResponseEntity<List<Exhibition>> getExhibitionsByOrg(@PathVariable Long orgId) {
        return ResponseEntity.ok(exhibitionService.getExhibitionsByOrg(orgId));
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
