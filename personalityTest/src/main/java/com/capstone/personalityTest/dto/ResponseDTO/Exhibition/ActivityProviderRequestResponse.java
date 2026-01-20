package com.capstone.personalityTest.dto.ResponseDTO.Exhibition;

import com.capstone.personalityTest.model.Enum.Exhibition.ActivityProviderRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityProviderRequestResponse {
    private Long id;
    private Long exhibitionId;
    private Long providerId;
    private String name;
    private String email;
    private ActivityProviderRequestStatus status;
    private String orgRequirements;
    private String providerProposal;
    private Integer proposedBoothsCount;
    private BigDecimal totalCost;
    private LocalDateTime responseDeadline;
    private String orgResponse;
    private LocalDateTime invitedAt;
    private LocalDateTime proposedAt;
    private LocalDateTime reviewedAt;
    private LocalDateTime approvedAt;
}
