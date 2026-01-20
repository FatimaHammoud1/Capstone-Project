package com.capstone.personalityTest.dto.ResponseDTO.Exhibition;

import com.capstone.personalityTest.model.Enum.Exhibition.OrganizationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationResponse {
    private Long id;
    private String name;
    private String description;
    private OrganizationType type;
    private Boolean active;
    private Long ownerId;
}
