package com.capstone.personalityTest.model.Exhibition;

import com.capstone.personalityTest.model.Enum.Exhibition.ParticipationStatus;
import com.capstone.personalityTest.model.Enum.Exhibition.PaymentStatus;
import com.capstone.personalityTest.converter.BoothDetailsConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UniversityParticipation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // primary key

    @ManyToOne
    @JoinColumn(name = "exhibition_id")
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Exhibition exhibition; // related exhibition

    @ManyToOne
    @JoinColumn(name = "university_id")
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private University university; // participating university

    @Enumerated(EnumType.STRING)
    private ParticipationStatus status; // participation lifecycle

    private Integer approvedBoothsCount; // allowed booths

    @Convert(converter = BoothDetailsConverter.class)
    @Column(columnDefinition = "TEXT")
    private Map<Long, Map<String, Object>> boothDetails; // booth info as JSON

    private BigDecimal participationFee; // total fee

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus; // paid or unpaid

    private LocalDateTime paymentDate; // payment time

    private LocalDateTime responseDeadline; // invitation deadline

    private LocalDateTime invitedAt; // invitation time

    private LocalDateTime registeredAt; // registration time

    private LocalDateTime confirmedAt; // final confirmation
}
