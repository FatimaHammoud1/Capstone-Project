package com.capstone.personalityTest.dto.ResponseDTO.Exhibition;

import com.capstone.personalityTest.model.Enum.Exhibition.ExhibitionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExhibitionResponse {
    private Long id;
    private Long organizationId;
    private String title;
    private String description;
    private String theme;
    private ExhibitionStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer totalAvailableBooths;
    private Double standardBoothSqm;
    private Integer maxBoothsPerUniversity;
    private Integer maxBoothsPerProvider;
    private Integer expectedVisitors;
    private Integer actualVisitors;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime finalizationDeadline;
}
