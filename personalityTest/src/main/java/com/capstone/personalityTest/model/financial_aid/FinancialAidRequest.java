package com.capstone.personalityTest.model.financial_aid;

import com.capstone.personalityTest.model.Exhibition.Organization;
import com.capstone.personalityTest.model.UserInfo;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FinancialAidRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private UserInfo student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private Organization organization;

    @Enumerated(EnumType.STRING)
    private Status status;

    @NotBlank(message = "Student name is required")
    private String studentName;

    @NotBlank(message = "Student phone is required")
    private String studentPhone;
    
    @NotNull(message = "Requested amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Requested amount must be greater than 0")
    private BigDecimal requestedAmount;

    private BigDecimal approvedAmount;
    
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
    
    @Column(columnDefinition = "TEXT")
    @NotBlank(message = "Reason is required")
    private String reason;
    
    @Column(columnDefinition = "TEXT")
    private String rejectionReason;
    
    private LocalDateTime requestedAt;
    private LocalDateTime reviewedAt;

    public enum Status {
        PENDING,
        APPROVED,
        REJECTED,
        CANCELLED
    }
}
