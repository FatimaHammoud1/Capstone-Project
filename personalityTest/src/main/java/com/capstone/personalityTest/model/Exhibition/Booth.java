package com.capstone.personalityTest.model.Exhibition;

import com.capstone.personalityTest.model.Enum.Exhibition.BoothType;
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
public class Booth {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // primary key

    @ManyToOne
    @JoinColumn(name = "exhibition_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Exhibition exhibition; // related exhibition

    @Enumerated(EnumType.STRING)
    private BoothType boothType; // university or activity provider

    private Long activityProviderRequestId; // source request (if activity)

    private Long universityParticipationId; // source participation (if university)

    private Integer durationMinutes; // session duration
    
    private String zone; // exhibition zone (e.g., "Zone A")

    private Integer boothNumber; // booth number
    
    private Integer maxParticipants; // allowed participants per session

    @ManyToOne
    @JoinColumn(name = "linked_activity_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Activity activity;

    private LocalDateTime createdAt; // creation time
}
