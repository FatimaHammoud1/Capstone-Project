package com.capstone.personalityTest.dto.RequestDTO.TestRequest;

import com.capstone.personalityTest.model.Enum.TargetGender;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubQuestionRequest{
//    private Long questionId;
    private String subQuestionText;
    private TargetGender targetGender;
    private Long metricId;
}
