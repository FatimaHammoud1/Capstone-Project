package com.capstone.personalityTest.dto.ResponseDTO.TestAttemptResponse;


import com.capstone.personalityTest.model.PersonalityResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestAttemptWithAnswersResponse {
    private Long attemptId;
    private Long testId;
    private String testTitle;
    private Long studentId;
    private String studentName;
    private List<AnswerResponse> answers;
    private PersonalityResult personalityResult;

}
