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

    private Long exhibitionId; // related exhibition

    private Long providerId; // invited activity provider

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
    private String rejectionReason; // rejection explanation

    private LocalDateTime invitedAt; // invitation time

    private LocalDateTime proposedAt; // proposal submission time

    private LocalDateTime reviewedAt; // organizer review time

    private LocalDateTime approvedAt; // approval time
}
