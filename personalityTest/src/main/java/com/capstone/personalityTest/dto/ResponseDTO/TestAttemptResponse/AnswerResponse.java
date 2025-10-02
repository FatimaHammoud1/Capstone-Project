package com.capstone.personalityTest.dto.ResponseDTO.TestAttemptResponse;

import com.capstone.personalityTest.model.Enum.AnswerType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnswerResponse {
    private Long questionId;
    private String questionText;
    private Long subQuestionId;
    private String subQuestionText;
    private AnswerType answerType;
    private Boolean binaryValue;
    private Integer scaleValue;
    private List<String> openValues;
}
