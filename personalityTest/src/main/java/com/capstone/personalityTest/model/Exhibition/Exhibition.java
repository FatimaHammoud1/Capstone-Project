package com.capstone.personalityTest.model.Exhibition;

import com.capstone.personalityTest.model.Enum.Exhibition.ExhibitionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Exhibition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // primary key

    private Long organizationId; // organization that owns the exhibition

    private String title; // exhibition public name

    @Column(columnDefinition = "TEXT")
    private String description; // detailed explanation of the exhibition

    private String theme; // main focus area (tech, health, engineering…)

    @Enumerated(EnumType.STRING)
    private ExhibitionStatus status; // current lifecycle state

    private LocalDate startDate; // exhibition start date

    private LocalDate endDate; // exhibition end date

    private LocalTime startTime; // daily start time

    private LocalTime endTime; // daily end time

    private Integer maxCapacity; // max allowed visitors overall

    private Integer expectedVisitors; // estimated number of visitors

    @Column(columnDefinition = "TEXT")
    private String scheduleJson; // generated schedule (booths & times)

    private LocalDateTime createdAt; // creation timestamp

    private LocalDateTime updatedAt; // last update timestamp
}
