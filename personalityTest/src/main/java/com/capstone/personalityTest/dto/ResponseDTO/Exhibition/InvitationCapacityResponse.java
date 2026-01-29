package com.capstone.personalityTest.dto.ResponseDTO.Exhibition;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvitationCapacityResponse {
    private Integer maxBoothsPerUniversity;
    private Integer maxBoothsPerProvider;
    private Integer maxUniversitiesToInvite; // calculated: remainingBooths / maxBoothsPerUniversity
    private Integer maxProvidersToInvite; // calculated: remainingBooths / maxBoothsPerProvider
    private Integer totalAvailableBooths;
    private Integer remainingBooths;
}
