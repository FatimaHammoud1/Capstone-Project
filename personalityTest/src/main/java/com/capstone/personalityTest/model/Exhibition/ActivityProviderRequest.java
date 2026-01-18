package com.capstone.personalityTest.model.Exhibition;

import com.capstone.personalityTest.model.Enum.Exhibition.ActivityProviderRequestStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityProviderRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // primary key

    @ManyToOne
    @JoinColumn(name = "exhibition_id")
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Exhibition exhibition; // related exhibition

    @ManyToOne
    @JoinColumn(name = "provider_id")
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private ActivityProvider provider; // invited activity provider

    @Enumerated(EnumType.STRING)
    private ActivityProviderRequestStatus status; // invitation lifecycle

    @Column(columnDefinition = "TEXT")
    private String orgRequirements; // organizer expectations

    @Column(columnDefinition = "TEXT")
    private String providerProposal; // provider’s proposal text

    private Integer proposedBoothsCount; // requested number of booths

    private BigDecimal totalCost; // proposed cost

    private LocalDateTime responseDeadline; // deadline to respond

    @Column(columnDefinition = "TEXT")
    private String orgResponse; // organizer response (approval note or rejection reason)

    private LocalDateTime invitedAt; // invitation time

    private LocalDateTime proposedAt; // proposal submission time

    private LocalDateTime reviewedAt; // organizer review time

    private LocalDateTime approvedAt; // approval time

    @ManyToMany
    @JoinTable(
        name = "request_activities", 
        joinColumns = @JoinColumn(name = "request_id"), 
        inverseJoinColumns = @JoinColumn(name = "activity_id"))
    private java.util.Set<Activity> proposedActivities = new java.util.HashSet<>();
}
