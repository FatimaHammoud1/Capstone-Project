package com.capstone.personalityTest.dto.ResponseDTO.test;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AIResultResponseDTO {
    private Long id;
    private Long testAttemptId;
    private String personalityCode;
    private String careerRecommendations;
    private String learningPath;
    private String jobMatches;
    private boolean emailSent;
    private LocalDateTime createdAt;
}
