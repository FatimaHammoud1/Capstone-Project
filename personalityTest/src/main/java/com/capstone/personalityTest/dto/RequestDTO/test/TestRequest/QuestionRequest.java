package com.capstone.personalityTest.dto.RequestDTO.test.TestRequest;

import com.capstone.personalityTest.model.Enum.AnswerType;
import com.capstone.personalityTest.model.Enum.TargetGender;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionRequest{
//  private Long sectionId;
    private String questionText;
    private AnswerType answerType;
    private TargetGender targetGender;
//  private List<SubQuestionRequest> subQuestions;
}

