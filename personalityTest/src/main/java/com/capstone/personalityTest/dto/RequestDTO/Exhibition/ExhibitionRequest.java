package com.capstone.personalityTest.dto.RequestDTO.Exhibition;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExhibitionRequest {
    private String title;
    private String description;
    private String theme;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Double standardBoothSqm; // booth size in sqm (e.g., 9.0)
    private Integer expectedVisitors;
    private String scheduleJson;
}
