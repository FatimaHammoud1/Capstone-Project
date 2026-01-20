package com.capstone.personalityTest.dto.ResponseDTO.Exhibition;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UniversityResponse {
    private Long id;
    private String name;
    private String contactEmail;
    private String contactPhone;
    private Boolean active;
    private Long ownerId; // just the ID, not full object
}
