package com.capstone.personalityTest.dto.ResponseDTO.Exhibition;

import com.capstone.personalityTest.model.Enum.Exhibition.ParticipationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class SchoolParticipationResponse {
    private Long id;
    private Long exhibitionId;
    private Long schoolId;
    private String schoolName;
    private String contactEmail;
    private ParticipationStatus status;
    private Integer expectedStudents;
    private LocalDateTime responseDeadline;
    private LocalDateTime invitedAt;
    private LocalDateTime acceptedAt;
    private String rejectionReason;

}
