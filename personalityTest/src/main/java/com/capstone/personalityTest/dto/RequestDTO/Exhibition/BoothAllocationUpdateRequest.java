package com.capstone.personalityTest.dto.RequestDTO.Exhibition;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BoothAllocationUpdateRequest {
    private Long boothId;
    private String zone;
    private Integer boothNumber;
}
