package com.capstone.personalityTest.controller.Exhibition;

import com.capstone.personalityTest.model.Exhibition.Exhibition;
import com.capstone.personalityTest.service.Exhibition.ExhibitionFinanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.capstone.personalityTest.dto.ResponseDTO.Exhibition.ExhibitionResponse;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/exhibitions")
@RequiredArgsConstructor
public class ExhibitionFinanceController {

    private final ExhibitionFinanceService financeService;

    // ----------------- CONFIRM EXHIBITION & CALCULATE PAYMENTS -----------------
    @PostMapping("/confirm/{exhibitionId}")
    @PreAuthorize("hasAnyRole('ORG_OWNER', 'DEVELOPER')")
    public ResponseEntity<ExhibitionResponse> confirmExhibition(
            @PathVariable Long exhibitionId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime finalizationDeadline,
            @AuthenticationPrincipal UserDetails userDetails) {

        ExhibitionResponse confirmed = financeService.confirmExhibition(
                exhibitionId, finalizationDeadline, userDetails.getUsername()
        );
        return ResponseEntity.ok(confirmed);
    }
}
