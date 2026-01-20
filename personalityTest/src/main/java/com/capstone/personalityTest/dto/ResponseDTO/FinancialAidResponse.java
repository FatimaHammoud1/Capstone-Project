package com.capstone.personalityTest.dto.ResponseDTO;

import com.capstone.personalityTest.model.financial_aid.FinancialAidRequest.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FinancialAidResponse {
    private Long id;
    private Long studentId;
    private Long organizationId;
    private String organizationName; // Added
    private String studentName; 
    private Status status;
    private BigDecimal requestedAmount;
    private BigDecimal approvedAmount; // Added
    private LocalDateTime requestedAt;
    private LocalDateTime reviewedAt; // Added
}
