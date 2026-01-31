package com.capstone.personalityTest.controller.Exhibition;

import com.capstone.personalityTest.dto.ResponseDTO.Exhibition.ExhibitionFinancialResponse;
import com.capstone.personalityTest.dto.ResponseDTO.Exhibition.RecommendedFeeResponse;
import com.capstone.personalityTest.model.Exhibition.Exhibition;
import com.capstone.personalityTest.service.Exhibition.ExhibitionFinanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.capstone.personalityTest.dto.ResponseDTO.Exhibition.ExhibitionResponse;
import com.capstone.personalityTest.model.Exhibition.ExhibitionFinancial;
import org.springframework.format.annotation.DateTimeFormat;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/exhibitions")
@RequiredArgsConstructor
public class ExhibitionFinanceController {

    private final ExhibitionFinanceService financeService;

    // ----------------- CALCULATE/RECALCULATE FINANCIALS -----------------
    @PostMapping("/{exhibitionId}/calculate-financials")
    @PreAuthorize("hasAnyRole('ORG_OWNER', 'DEVELOPER')")
    public ResponseEntity<ExhibitionFinancialResponse> calculateFinancials(
            @PathVariable Long exhibitionId) {

        ExhibitionFinancialResponse financial = financeService.calculateFinancials(exhibitionId);
        return ResponseEntity.ok(financial);
    }

    // ----------------- GET FINANCIAL REPORT -----------------
    @GetMapping("/{exhibitionId}/financial-report")
    @PreAuthorize("hasAnyRole('ORG_OWNER', 'DEVELOPER')")
    public ResponseEntity<ExhibitionFinancialResponse> getFinancialReport(
            @PathVariable Long exhibitionId) {

        ExhibitionFinancialResponse financial = financeService.getFinancialReport(exhibitionId);
        return ResponseEntity.ok(financial);
    }

    // ----------------- GET RECOMMENDED UNIVERSITY FEE -----------------
    @GetMapping("/{exhibitionId}/recommended-fee")
    @PreAuthorize("hasAnyRole('ORG_OWNER', 'DEVELOPER')")
    public ResponseEntity<RecommendedFeeResponse> getRecommendedFee(
            @PathVariable Long exhibitionId,
            @RequestParam int expectedUniversityCount,
            @RequestParam(defaultValue = "0.15") BigDecimal profitMarginPercentage) {

        RecommendedFeeResponse response = 
                financeService.calculateRecommendedFee(exhibitionId, expectedUniversityCount, profitMarginPercentage);
        return ResponseEntity.ok(response);
    }
}
