package com.capstone.personalityTest.dto.ResponseDTO.test.TestAttemptResponse;

import com.capstone.personalityTest.dto.ResponseDTO.test.TestResponse.SectionResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestAttemptResponse {
    private Long Id;
    private Long testId;
    private String testTitle;
    private String testDescription;
    private List<SectionResponse> sections;
}
