package com.capstone.personalityTest.dto.RequestDTO.test.TestAttemptRequest;

import com.capstone.personalityTest.model.Enum.AnswerType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnswerRequest {
    private Long questionId;
    private Long subQuestionId; // optional
    private AnswerType answerType;  // "BINARY", "SCALE", "OPEN"
    private Boolean binaryValue;
    private Integer scaleValue;
    private List<String> openValues;
}

