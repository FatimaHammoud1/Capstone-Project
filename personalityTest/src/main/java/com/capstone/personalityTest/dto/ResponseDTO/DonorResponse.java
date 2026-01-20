package com.capstone.personalityTest.dto.ResponseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DonorResponse {
    private Long id;
    private String name;
    private BigDecimal totalBudget;
    private BigDecimal availableBudget;
    private Boolean active;
    
}
