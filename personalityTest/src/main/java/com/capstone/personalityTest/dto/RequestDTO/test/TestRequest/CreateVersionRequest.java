package com.capstone.personalityTest.dto.RequestDTO.test.TestRequest;

import lombok.Data;

@Data
public class CreateVersionRequest {
    private Long baseTestId;
    private Long sourceTestId; // nullable (if first version)
    private String versionName;
}

