package com.capstone.personalityTest.dto.ResponseDTO.financial_aid;

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
    private String organizationName;
    private String studentName;
    private String studentPhone; 
    private BigDecimal requestedAmount;
    private BigDecimal approvedAmount;
    private Status status;
    private Double gpa;
    private String fieldOfStudy; 
    private String universityName;
    private BigDecimal familyIncome; 
    private Documents documents;
    private String reason;
    private String rejectionReason; 
    private Long donorId;           
    private String donorName;       
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
