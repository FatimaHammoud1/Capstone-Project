package com.capstone.personalityTest.dto.ResponseDTO.test.TestResponse;

import com.capstone.personalityTest.model.Enum.TestStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestResponse {
    private Long id;
    private String title;
    private String description;
    private TestStatus status;
    private boolean active;
    private String versionName;
    private List<SectionResponse> sections;
}

