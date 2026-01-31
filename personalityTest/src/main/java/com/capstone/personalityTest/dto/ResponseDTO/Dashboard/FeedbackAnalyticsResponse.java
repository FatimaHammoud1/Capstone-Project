package com.capstone.personalityTest.dto.ResponseDTO.Dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackAnalyticsResponse {
    private Long totalFeedbacks;
    private Double averageRating;
    private Map<Integer, Long> feedbacksByRating; // Rating (1-5) -> Number of students
}
