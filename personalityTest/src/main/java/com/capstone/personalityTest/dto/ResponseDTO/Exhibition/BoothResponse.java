package com.capstone.personalityTest.dto.ResponseDTO.Exhibition;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
public class BoothResponse {
    private Long id;
    private Long exhibitionId;
    private String type; // UNIVERSITY/ACTIVITY_PROVIDER
    private Long universityParticipationId;
    private Long activityProviderRequestId;
    private Long activityId;
    private String zone;
    private Integer boothNumber;
    private Integer durationMinutes;
    private Integer maxParticipants;
    private LocalDateTime createdAt;
}
