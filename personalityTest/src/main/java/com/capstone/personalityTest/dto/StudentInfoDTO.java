package com.capstone.personalityTest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO containing student information for AI analysis.
 * Used as part of CompleteAIRequest to provide context for personalized recommendations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentInfoDTO {

    /**
     * Student's full name
     * Used in email personalization
     */
    private String name;

    /**
     * Student's email address
     * Used to send career guidance email
     */
    private String email;

    /**
     * Student's gender (MALE/FEMALE)
     * May be used for gender-specific career recommendations
     */
    private String gender;
}
