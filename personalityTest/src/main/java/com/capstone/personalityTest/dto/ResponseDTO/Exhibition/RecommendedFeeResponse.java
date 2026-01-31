package com.capstone.personalityTest.dto.ResponseDTO.Exhibition;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecommendedFeeResponse {
    private BigDecimal totalExpectedCosts;
    private BigDecimal venueRentalCost;
    private BigDecimal activityProviderCosts;
    private int expectedUniversityCount; // mentioned by org owner
    private BigDecimal breakEvenFeePerUniversity;
    private BigDecimal profitMarginPercentage;
    private BigDecimal recommendedFeePerUniversity;
}
