package com.capstone.personalityTest.model.Exhibition;

import com.capstone.personalityTest.model.Enum.Exhibition.VenueRequestStatus;
import com.capstone.personalityTest.model.UserInfo;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VenueRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // primary key

    @ManyToOne
    @JoinColumn(name = "exhibition_id")
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Exhibition exhibition; // related exhibition

    @ManyToOne
    @JoinColumn(name = "venue_id")
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Venue venue; // requested venue

    @Enumerated(EnumType.STRING)
    private VenueRequestStatus status; // approval status

    @Column(columnDefinition = "TEXT")
    private String orgNotes; // notes from organization

    @Column(columnDefinition = "TEXT")
    private String municipalityResponse; // response notes

    private LocalDateTime responseDeadline; // deadline to approve/reject

    private LocalDateTime requestedAt; // request creation time

    private LocalDateTime reviewedAt; // decision time

    @ManyToOne
    @JoinColumn(name = "reviewed_by_user_id")
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private UserInfo reviewedByUser; // municipality admin user
}
