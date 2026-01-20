package com.capstone.personalityTest.dto.RequestDTO;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FinancialAidReviewRequest {
    
    @NotBlank(message = "Decision is required (APPROVE or REJECT)")
    private String decision; // APPROVE, REJECT

    private BigDecimal approvedAmount; // Required for APPROVE

    private String rejectionReason; // Required for REJECT
}
