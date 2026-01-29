package com.capstone.personalityTest.dto.ResponseDTO.Exhibition;

import com.capstone.personalityTest.model.Enum.Exhibition.StudentRegistrationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentRegistrationResponse {
    private Long id;
    private Long exhibitionId;
    private String exhibitionTitle;
    private Long studentId;
    private String studentName;
    private String studentEmail;
    private StudentRegistrationStatus status;
    private boolean approved;
    private LocalDateTime registeredAt;
    private LocalDateTime approvedAt;
    private LocalDateTime attendedAt;
}
