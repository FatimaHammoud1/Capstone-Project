package com.capstone.personalityTest.dto.ResponseDTO.TestResponse;


import com.capstone.personalityTest.model.Test.Metric;
import com.capstone.personalityTest.model.Enum.TargetGender;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubQuestionResponse {
    private Long id;
    private String subQuestionText;
    private TargetGender targetGender;
    private Metric metric;
}
