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

import com.capstone.personalityTest.model.Exhibition.UniversityParticipation;
import com.capstone.personalityTest.model.Exhibition.SchoolParticipation;
import com.capstone.personalityTest.model.Exhibition.ActivityProviderRequest;
import com.capstone.personalityTest.model.Enum.Exhibition.ExhibitionStatus;
import com.capstone.personalityTest.repository.Exhibition.UniversityParticipationRepository;
import com.capstone.personalityTest.repository.Exhibition.SchoolParticipationRepository;
import com.capstone.personalityTest.repository.Exhibition.ActivityProviderRequestRepository;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final StudentRegistrationRepository registrationRepository;
    private final ExhibitionRepository exhibitionRepository;
    private final UserInfoRepository userInfoRepository;
    private final ExhibitionService exhibitionService;
    private final UniversityParticipationRepository universityParticipationRepository;
    private final SchoolParticipationRepository schoolParticipationRepository;
    private final ActivityProviderRequestRepository activityProviderRequestRepository;

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

        // Validate exhibition is ACTIVE
        if (exhibition.getStatus() != ExhibitionStatus.ACTIVE) {
            throw new RuntimeException("Can only mark attendance during ACTIVE exhibition");
        }

        // Only approved students can be marked
        if (!Boolean.TRUE.equals(registration.getApproved())) {
            throw new RuntimeException("Cannot mark attendance for unapproved registration");
        }

        // Update status
        registration.setStatus(attended ? StudentRegistrationStatus.ATTENDED : StudentRegistrationStatus.NO_SHOW);
        registration.setAttendedAt(LocalDateTime.now());

        StudentRegistration saved = registrationRepository.save(registration);
        
        // Update exhibition's actual visitors count (only if attended)
        if (attended) {
            exhibitionService.updateActualVisitors(exhibition.getId());
        }
        
        return mapToResponse(saved);
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
    
    // ----------------- Mark University Attendance -----------------
    public void markUniversityAttendance(Long participationId, String orgOwnerEmail) {
        UniversityParticipation participation = universityParticipationRepository.findById(participationId)
                .orElseThrow(() -> new RuntimeException("University participation not found"));

        Exhibition exhibition = participation.getExhibition();

        // Validate org owner
        UserInfo orgOwner = userInfoRepository.findByEmail(orgOwnerEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean isDev = orgOwner.getRoles().stream().anyMatch(r -> r.getCode().equals("DEVELOPER"));
        if (!exhibition.getOrganization().getOwner().getId().equals(orgOwner.getId()) && !isDev) {
            throw new RuntimeException("Only the organization owner can mark attendance");
        }

        // Validate exhibition is ACTIVE
        if (exhibition.getStatus() != ExhibitionStatus.ACTIVE) {
            throw new RuntimeException("Can only mark attendance during ACTIVE exhibition");
        }

        // Set attendance
        participation.setAttendedAt(LocalDateTime.now());
        participation.setStatus(ParticipationStatus.ATTENDED);
        universityParticipationRepository.save(participation);

        // Update actual visitors
        exhibitionService.updateActualVisitors(exhibition.getId());
    }
    
    // ----------------- Mark School Attendance -----------------
    public void markSchoolAttendance(Long participationId, String orgOwnerEmail) {
        SchoolParticipation participation = schoolParticipationRepository.findById(participationId)
                .orElseThrow(() -> new RuntimeException("School participation not found"));

        Exhibition exhibition = participation.getExhibition();

        // Validate org owner
        UserInfo orgOwner = userInfoRepository.findByEmail(orgOwnerEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean isDev = orgOwner.getRoles().stream().anyMatch(r -> r.getCode().equals("DEVELOPER"));
        if (!exhibition.getOrganization().getOwner().getId().equals(orgOwner.getId()) && !isDev) {
            throw new RuntimeException("Only the organization owner can mark attendance");
        }

        // Validate exhibition is ACTIVE
        if (exhibition.getStatus() != ExhibitionStatus.ACTIVE) {
            throw new RuntimeException("Can only mark attendance during ACTIVE exhibition");
        }

        // Set attendance
        participation.setAttendedAt(LocalDateTime.now());
        participation.setStatus(ParticipationStatus.ATTENDED);
        schoolParticipationRepository.save(participation);

        // Update actual visitors
        exhibitionService.updateActualVisitors(exhibition.getId());
    }
    
    // ----------------- Mark Provider Attendance -----------------
    public void markProviderAttendance(Long requestId, String orgOwnerEmail) {
        ActivityProviderRequest request = activityProviderRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Activity provider request not found"));

        Exhibition exhibition = request.getExhibition();

        // Validate org owner
        UserInfo orgOwner = userInfoRepository.findByEmail(orgOwnerEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean isDev = orgOwner.getRoles().stream().anyMatch(r -> r.getCode().equals("DEVELOPER"));
        if (!exhibition.getOrganization().getOwner().getId().equals(orgOwner.getId()) && !isDev) {
            throw new RuntimeException("Only the organization owner can mark attendance");
        }

        // Validate exhibition is ACTIVE
        if (exhibition.getStatus() != ExhibitionStatus.ACTIVE) {
            throw new RuntimeException("Can only mark attendance during ACTIVE exhibition");
        }

        // Set attendance
        request.setAttendedAt(LocalDateTime.now());
        request.setStatus(ActivityProviderRequestStatus.ATTENDED);
        activityProviderRequestRepository.save(request);

        // Update actual visitors
        exhibitionService.updateActualVisitors(exhibition.getId());
    }
}
