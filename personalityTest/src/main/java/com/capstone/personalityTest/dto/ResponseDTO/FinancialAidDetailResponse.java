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
public class FinancialAidDetailResponse {
    private Long id;
    private String studentName;
    private String studentPhone; // Added
    private BigDecimal requestedAmount;
    private BigDecimal approvedAmount;
    private Status status;
    private Double gpa;
    private String fieldOfStudy; // Added
    private String universityName;
    private BigDecimal familyIncome; // Added
    private Documents documents;
    private String reason;
    private LocalDateTime requestedAt;
    private LocalDateTime reviewedAt;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Documents {
        private String idCard;
        private String fees;
        private String grades;
    }
}
