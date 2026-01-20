package com.capstone.personalityTest.dto.RequestDTO.Exhibition;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BoothLimitsRequest {
    private Integer maxBoothsPerUniversity;
    private Integer maxBoothsPerProvider;
}
