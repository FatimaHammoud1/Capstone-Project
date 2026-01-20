package com.capstone.personalityTest.model.Exhibition;

import com.capstone.personalityTest.model.Enum.Exhibition.ParticipationStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SchoolParticipation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // primary key

    @ManyToOne
    @JoinColumn(name = "exhibition_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Exhibition exhibition; // related exhibition

    @ManyToOne
    @JoinColumn(name = "school_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private School school; // invited school

    @Enumerated(EnumType.STRING)
    private ParticipationStatus status; // participation status

    private Integer expectedVisitors; // number of visitors (students)

    private LocalDateTime responseDeadline; // acceptance deadline

    private LocalDateTime invitedAt; // invitation time

    private LocalDateTime acceptedAt; // acceptance time

    private LocalDateTime registeredAt;

    private String rejectionReason;

    private LocalDateTime confirmedAt; // final confirmation

    private LocalDateTime finalizedAt; // finalization time

    private LocalDateTime attendedAt; // attendance time (when school actually attended)
}
