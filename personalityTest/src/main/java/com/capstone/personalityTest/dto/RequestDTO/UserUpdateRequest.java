package com.capstone.personalityTest.dto.RequestDTO;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {

    private String name;
    
    private String password;

    private com.capstone.personalityTest.model.Enum.TargetGender gender;
}
