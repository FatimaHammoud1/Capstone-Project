package com.capstone.personalityTest.dto.RequestDTO.test;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MetricRequest {
    @NotBlank(message = "Code is required")
    private String code;

    @NotBlank(message = "Label is required")
    private String label;

    private String description;

    private Long baseTestId;
}
