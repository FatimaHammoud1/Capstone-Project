package com.capstone.personalityTest.dto.RequestDTO;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FinancialAidApplyRequest {

    @NotNull(message = "Organization ID is required")
    private Long organizationId;

    @NotBlank(message = "Student name is required")
    private String studentName;

    @NotBlank(message = "Student phone is required")
    private String studentPhone;

    @NotNull(message = "Requested amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Requested amount must be greater than 0")
    private BigDecimal requestedAmount;

    @NotNull(message = "GPA is required")
    @DecimalMin(value = "0.0", message = "GPA must be at least 0.0")
    @DecimalMax(value = "4.0", message = "GPA must be at most 4.0")
    private Double gpa;

    @NotBlank(message = "Field of study is required")
    private String fieldOfStudy;

    @NotBlank(message = "University name is required")
    private String universityName;

    @NotNull(message = "Family income is required")
    private BigDecimal familyIncome;

    @NotBlank(message = "ID card URL is required")
    private String idCardUrl;

    @NotBlank(message = "University fees URL is required")
    private String universityFeesUrl;

    @NotBlank(message = "Grade proof URL is required")
    private String gradeProofUrl;

    @NotBlank(message = "Reason is required")
    private String reason;
}
