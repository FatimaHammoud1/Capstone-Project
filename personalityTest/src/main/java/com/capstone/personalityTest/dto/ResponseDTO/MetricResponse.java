package com.capstone.personalityTest.dto.ResponseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MetricResponse {
    private Long id;
    private String code;
    private String label;
    private String description;
    private Long baseTestId;
    private String baseTestCode;
}
