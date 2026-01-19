package com.capstone.personalityTest.service.Exhibition;

import com.capstone.personalityTest.model.Enum.Exhibition.StudentRegistrationStatus;
import com.capstone.personalityTest.model.Exhibition.Exhibition;
import com.capstone.personalityTest.model.Exhibition.StudentRegistration;
import com.capstone.personalityTest.model.UserInfo;
import com.capstone.personalityTest.repository.Exhibition.ExhibitionRepository;
import com.capstone.personalityTest.repository.Exhibition.StudentRegistrationRepository;
import com.capstone.personalityTest.repository.UserInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.capstone.personalityTest.dto.ResponseDTO.Exhibition.StudentRegistrationResponse;

@Service
@RequiredArgsConstructor
public class StudentAttendanceService {

    private final StudentRegistrationRepository registrationRepository;
    private final ExhibitionRepository exhibitionRepository;
    private final UserInfoRepository userInfoRepository;

    // ----------------- Mark Attendance -----------------
    public StudentRegistrationResponse markAttendance(Long registrationId, boolean attended, String orgOwnerEmail) {
        StudentRegistration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new RuntimeException("Registration not found"));

        Exhibition exhibition = registration.getExhibition();

        // Only ORG_OWNER of this exhibition can mark attendance
        UserInfo orgOwner = userInfoRepository.findByEmail(orgOwnerEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean isDev = orgOwner.getRoles().stream().anyMatch(r -> r.getCode().equals("DEVELOPER"));
        if (!exhibition.getOrganization().getOwner().getId().equals(orgOwner.getId()) && !isDev) {
            throw new RuntimeException("Only the organization owner can mark attendance");
        }

        // Only approved students can be marked
        if (!Boolean.TRUE.equals(registration.getApproved())) {
            throw new RuntimeException("Cannot mark attendance for unapproved registration");
        }

        // Update status
        registration.setStatus(attended ? StudentRegistrationStatus.ATTENDED : StudentRegistrationStatus.NO_SHOW);
        registration.setAttendedAt(LocalDateTime.now());

        return mapToResponse(registrationRepository.save(registration));
    }

    // ----------------- Mark Attendance for Multiple Students -----------------
    public List<StudentRegistrationResponse> markAttendanceMultiple(List<Long> registrationIds, boolean attended, String orgOwnerEmail) {
        List<StudentRegistrationResponse> updatedList = new ArrayList<>();
        for (Long id : registrationIds) {
            updatedList.add(markAttendance(id, attended, orgOwnerEmail));
        }
        return updatedList;
    }

    private StudentRegistrationResponse mapToResponse(StudentRegistration registration) {
        return new StudentRegistrationResponse(
            registration.getId(),
            registration.getExhibition().getId(),
            registration.getExhibition().getTitle(),
            registration.getStudent().getId(),
            registration.getStudent().getName(), 
            registration.getStudent().getEmail(),
            registration.getStatus(),
            Boolean.TRUE.equals(registration.getApproved()),
            registration.getRegisteredAt(),
            registration.getApprovedAt(),
            registration.getAttendedAt()
        );
    }
}
