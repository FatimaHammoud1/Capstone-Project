package com.capstone.personalityTest.dto.RequestDTO.TestRequest;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTestRequest {
    private String title;
    private String description;
}
