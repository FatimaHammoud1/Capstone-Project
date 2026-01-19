package com.capstone.personalityTest.dto.ResponseDTO.Exhibition;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExhibitionFeedbackResponse {
    private Long id;
    private Long exhibitionId;
    private Long studentId;
    private String studentName;
    private Integer rating;
    private String comments;
    private LocalDateTime createdAt;
}
