package com.capstone.personalityTest.dto.RequestDTO.TestRequest;

import com.capstone.personalityTest.model.Enum.AnswerType;
import com.capstone.personalityTest.model.Enum.TargetGender;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionRequest{
    private String questionText;
    private AnswerType answerType;
    private TargetGender targetGender;
    private List<SubQuestionRequest> subQuestions;
}

