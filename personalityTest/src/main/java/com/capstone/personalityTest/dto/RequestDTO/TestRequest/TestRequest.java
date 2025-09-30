package com.capstone.personalityTest.dto.RequestDTO.TestRequest;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestRequest {
    private String title;
    private String description;
//  private List<SectionRequest> sections;
}

