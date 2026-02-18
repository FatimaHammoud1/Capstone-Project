package com.capstone.personalityTest.model.Exhibition;

import com.capstone.personalityTest.model.Enum.Exhibition.ExhibitionStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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

    // Default booth size for all exhibitions (3x3 meters = 9 sqm)
    public static final Double DEFAULT_BOOTH_SQM = 9.0;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // primary key

    @ManyToOne
    @JoinColumn(name = "organization_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Organization organization; // organization that owns the exhibition

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

    // Booth capacity fields
    private Integer totalAvailableBooths; // calculated from venue space (venueSqm / standardBoothSqm)

    private Double standardBoothSqm; // optional: booth size override (defaults to DEFAULT_BOOTH_SQM if null)

    private Integer maxBoothsPerUniversity; // optional limit per university

    private Integer maxBoothsPerProvider; // optional limit per activity provider

    // Visitor tracking
    private Integer expectedVisitors; // org's initial estimate (not validated)

    private Integer actualVisitors; // actual number of visitors who attended (tracked during exhibition)


    private LocalDateTime createdAt; // creation timestamp

    private LocalDateTime updatedAt; // last update timestamp

    private LocalDateTime finalizationDeadline; // deadline for participants to finalize

    // Helper method to get the effective booth size (uses default if not specified)
    public Double getEffectiveBoothSqm() {
        return standardBoothSqm != null ? standardBoothSqm : DEFAULT_BOOTH_SQM;
    }
}
