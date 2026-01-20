package com.capstone.personalityTest.dto.ResponseDTO.Exhibition;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MunicipalityResponse {
    private Long id;
    private String name;
    private String region;
    private String contactEmail;
    private String contactPhone;
    private Long ownerId;
}
