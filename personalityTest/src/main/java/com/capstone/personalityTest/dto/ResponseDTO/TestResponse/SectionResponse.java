package com.capstone.personalityTest.dto.ResponseDTO.TestResponse;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SectionResponse{
    private Long id;
    private String title;
    private List<QuestionResponse> questions;
}
