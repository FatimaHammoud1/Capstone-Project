package com.capstone.personalityTest.model.Exhibition;

import com.capstone.personalityTest.model.Enum.Exhibition.StudentRegistrationStatus;
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

    private Long exhibitionId;

    private Long studentId;

    @Enumerated(EnumType.STRING)
    private StudentRegistrationStatus status;

    private LocalDateTime registeredAt;

    private LocalDateTime attendedAt;
}
