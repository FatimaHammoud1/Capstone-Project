package com.capstone.personalityTest.model.Exhibition;

import com.capstone.personalityTest.model.Enum.Exhibition.StudentRegistrationStatus;
import com.capstone.personalityTest.model.UserInfo;
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
public class StudentRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "exhibition_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Exhibition exhibition;

    @ManyToOne
    @JoinColumn(name = "student_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private UserInfo student;

    @Enumerated(EnumType.STRING)
    private StudentRegistrationStatus status;

    private Boolean approved; // new field

    private LocalDateTime approvedAt; // new field

    private LocalDateTime registeredAt;

    private LocalDateTime attendedAt;
}
