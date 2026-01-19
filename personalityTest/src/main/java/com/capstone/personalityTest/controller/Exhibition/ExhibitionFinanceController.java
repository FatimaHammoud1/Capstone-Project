package com.capstone.personalityTest.controller.Exhibition;

import com.capstone.personalityTest.model.Exhibition.Exhibition;
import com.capstone.personalityTest.service.Exhibition.ExhibitionFinanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/exhibitions")
@RequiredArgsConstructor
public class ExhibitionFinanceController {

    private final ExhibitionFinanceService financeService;

    // ----------------- CONFIRM EXHIBITION & CALCULATE PAYMENTS -----------------
    @PostMapping("/confirm/{exhibitionId}")
    @PreAuthorize("hasAnyRole('ORG_OWNER', 'DEVELOPER')")
    public ResponseEntity<com.capstone.personalityTest.dto.ResponseDTO.Exhibition.ExhibitionResponse> confirmExhibition(
            @PathVariable Long exhibitionId,
            @AuthenticationPrincipal UserDetails userDetails) {

        com.capstone.personalityTest.dto.ResponseDTO.Exhibition.ExhibitionResponse confirmed = financeService.confirmExhibition(
                exhibitionId, userDetails.getUsername()
        );
        return ResponseEntity.ok(confirmed);
    }
}
