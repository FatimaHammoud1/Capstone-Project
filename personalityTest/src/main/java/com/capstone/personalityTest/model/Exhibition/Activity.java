package com.capstone.personalityTest.model.Exhibition;

import com.capstone.personalityTest.model.Enum.Exhibition.ActivityType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Activity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    private ActivityType type;

    private Integer suggestedDurationMinutes;

    private Integer suggestedMaxParticipants;

    private Boolean active;

    @ManyToOne
    @JoinColumn(name = "provider_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private ActivityProvider provider;
}
