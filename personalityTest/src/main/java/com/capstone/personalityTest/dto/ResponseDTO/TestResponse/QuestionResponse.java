package com.capstone.personalityTest.dto.ResponseDTO.TestResponse;


import com.capstone.personalityTest.model.Enum.AnswerType;
import com.capstone.personalityTest.model.Enum.PersonalityTrait;
import com.capstone.personalityTest.model.Enum.TargetGender;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionResponse{
    private Long id;
    private String questionText;
    private AnswerType answerType;
    private TargetGender targetGender;
   // private List<SubQuestionResponse> subQuestions;
    private Map<PersonalityTrait, List<SubQuestionResponse>> groupedSubQuestions;
}
