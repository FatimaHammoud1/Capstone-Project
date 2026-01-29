package com.capstone.personalityTest.dto.ResponseDTO.test.TestAttemptResponse;

import com.capstone.personalityTest.model.Enum.AnswerType;
import com.capstone.personalityTest.model.testm.Test.Metric;
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
    private Metric metric;
    private AnswerType answerType;
    private Boolean binaryValue;
    private Integer scaleValue;
    private List<String> openValues;
}
