package com.capstone.personalityTest.model.Exhibition;

import com.capstone.personalityTest.model.Enum.Exhibition.BoothType;
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
    private Exhibition exhibition; // related exhibition

    @Enumerated(EnumType.STRING)
    private BoothType boothType; // university or activity provider

    private Long activityProviderRequestId; // source request (if activity)

    private Long universityParticipationId; // source participation (if university)

    private Long activityId; // linked activity

    private String boothNumber; // physical booth code

    private Integer maxParticipants; // allowed participants per session

    private Integer durationMinutes; // session duration

    private LocalDateTime createdAt; // creation time
}
