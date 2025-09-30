package com.capstone.personalityTest.dto.ResponseDTO;

import com.capstone.personalityTest.dto.ResponseDTO.TestResponse.SectionResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestAttemptResponse {
    private Long testId;
    private String testTitle;
    private String testDescription;
    private List<SectionResponse> sections;
}
