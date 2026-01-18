package com.capstone.personalityTest.dto.ResponseDTO.Exhibition;

import com.capstone.personalityTest.model.Enum.Exhibition.UniversityParticipationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class UniversityParticipationResponse {
    private Long id;
    private Long exhibitionId;
    private Long universityId;
    private String universityName;
    private String contactEmail;
    private UniversityParticipationStatus status;
    private Integer approvedBoothsCount;
    private String boothDetails;
    private BigDecimal participationFee;
    private String paymentStatus; // Assuming enum or string
    private LocalDateTime paymentDate;
    private LocalDateTime responseDeadline;
    private LocalDateTime invitedAt;
    private LocalDateTime registeredAt;
    private LocalDateTime confirmedAt;
}
