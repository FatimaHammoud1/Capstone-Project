package com.capstone.personalityTest.service.Exhibition;

import com.capstone.personalityTest.model.Enum.Exhibition.BoothType;
import com.capstone.personalityTest.model.Enum.Exhibition.ExhibitionStatus;
import com.capstone.personalityTest.model.Enum.Exhibition.StudentRegistrationStatus;
import com.capstone.personalityTest.model.Exhibition.Exhibition;
import com.capstone.personalityTest.model.Exhibition.StudentRegistration;
import com.capstone.personalityTest.model.UserInfo;
import com.capstone.personalityTest.repository.Exhibition.BoothRepository;
import com.capstone.personalityTest.repository.Exhibition.ExhibitionRepository;
import com.capstone.personalityTest.repository.Exhibition.StudentRegistrationRepository;
import com.capstone.personalityTest.repository.UserInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentRegistrationService {

    private final ExhibitionRepository exhibitionRepository;
    private final StudentRegistrationRepository registrationRepository;
    private final UserInfoRepository userInfoRepository;
    private final BoothRepository boothRepository;

    // ----------------- Register Student -----------------
    public StudentRegistration registerStudent(Long exhibitionId, String studentEmail) {
        UserInfo student = userInfoRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        Exhibition exhibition = exhibitionRepository.findById(exhibitionId)
                .orElseThrow(() -> new RuntimeException("Exhibition not found"));

        // Validation: exhibition must be ACTIVE
        if (exhibition.getStatus() != ExhibitionStatus.ACTIVE) {
            throw new RuntimeException("Students can register only for ACTIVE exhibitions");
        }

        // Validation: check if student already registered
        if (registrationRepository.existsByExhibitionIdAndStudentId(exhibitionId, student.getId())) {
            throw new RuntimeException("Student already registered for this exhibition");
        }

        // Validation: check capacity
        int totalRegistered = registrationRepository.countByExhibitionId(exhibitionId);

        // Sum of activity booth max participants
        int totalActivityParticipants = boothRepository.sumMaxParticipantsByExhibitionIdAndType(exhibitionId, BoothType.ACTIVITY_PROVIDER);

        // Sum of university booths participants
        int totalUniversityBooths = boothRepository.sumMaxParticipantsByExhibitionIdAndType(exhibitionId, BoothType.UNIVERSITY);

        int totalOccupied = totalRegistered + totalActivityParticipants + totalUniversityBooths;

        if (totalOccupied >= exhibition.getMaxCapacity()) {
            throw new RuntimeException("Exhibition capacity full, cannot register more students");
        }

        // Create registration
        StudentRegistration registration = new StudentRegistration();
        registration.setExhibition(exhibition);
        registration.setStudent(student);
        registration.setStatus(StudentRegistrationStatus.REGISTERED);
        registration.setRegisteredAt(LocalDateTime.now());
        registration.setApproved(false); // Default false

        return registrationRepository.save(registration);
    }

    // Optional: list all registrations for a student
    public List<StudentRegistration> getStudentRegistrations(String studentEmail) {
        UserInfo student = userInfoRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        return registrationRepository.findByStudentId(student.getId());
    }

    // ----------------- Approve Student -----------------
    public StudentRegistration approveStudent(Long registrationId, String orgOwnerEmail) {
        StudentRegistration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new RuntimeException("Registration not found"));

        Exhibition exhibition = registration.getExhibition();

        // Only ORG_OWNER of this exhibition can approve
        UserInfo orgOwner = userInfoRepository.findByEmail(orgOwnerEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!exhibition.getOrganization().getOwner().getId().equals(orgOwner.getId())) {
            boolean isDev = orgOwner.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_DEVELOPER"));
            if (!isDev) {
              throw new RuntimeException("Only the organization owner can approve registrations");
            }
        }

        // Only REGISTERED students can be approved
        if (registration.getStatus() != StudentRegistrationStatus.REGISTERED) {
            throw new RuntimeException("Only registered students can be approved");
        }

        // Capacity check
        int totalApproved = registrationRepository.countByExhibitionIdAndApprovedTrue(exhibition.getId());
        
        // Using BoothRepository for these sums as they are related to Booth entity
        int totalActivityParticipants = boothRepository.sumMaxParticipantsByExhibitionIdAndType(exhibition.getId(), BoothType.ACTIVITY_PROVIDER);
        int totalUniversityBooths = boothRepository.sumMaxParticipantsByExhibitionIdAndType(exhibition.getId(), BoothType.UNIVERSITY);

        int totalOccupied = totalApproved + totalActivityParticipants + totalUniversityBooths;

        if (totalOccupied >= exhibition.getMaxCapacity()) {
            throw new RuntimeException("Exhibition capacity reached, cannot approve more students");
        }

        // Approve registration
        registration.setApproved(true);
        registration.setApprovedAt(LocalDateTime.now()); 
        // Optionally update status if needed, but keeping it REGISTERED with approved flag for now as per logic, 
        // or could act as a secondary confirmation.

        return registrationRepository.save(registration);
    }

    // Optional: Approve multiple students at once
    public List<StudentRegistration> approveStudents(List<Long> registrationIds, String orgOwnerEmail) {
        List<StudentRegistration> approvedList = new ArrayList<>();
        for (Long id : registrationIds) {
            approvedList.add(approveStudent(id, orgOwnerEmail));
        }
        return approvedList;
    }
    
    // ----------------- Cancel Registration -----------------
    @Transactional
    public StudentRegistration cancelRegistration(Long registrationId, String cancellerEmail) {
        UserInfo canceller = userInfoRepository.findByEmail(cancellerEmail)
                .orElseThrow(() -> new RuntimeException("Canceller not found"));

        StudentRegistration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new RuntimeException("Registration not found"));

        Exhibition exhibition = registration.getExhibition();

        if (exhibition.getStatus() == ExhibitionStatus.ACTIVE) {
             throw new RuntimeException("Cannot cancel registration when exhibition is ACTIVE");
        }
        
        boolean isStudent = registration.getStudent().getId().equals(canceller.getId());
        boolean isOrgOwner = exhibition.getOrganization().getOwner().getId().equals(canceller.getId());

        boolean isDev = canceller.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_DEVELOPER"));

        if (!isStudent && !isOrgOwner && !isDev) {
            throw new RuntimeException("Not authorized to cancel this registration");
        }

        if (registration.getStatus() != StudentRegistrationStatus.REGISTERED) {
            throw new RuntimeException("Only REGISTERED registrations can be cancelled");
        }

        registration.setStatus(StudentRegistrationStatus.CANCELLED);
        // Side effects: Release seat - Implicit via count queries excluding CANCELLED
        
        return registrationRepository.save(registration);
    }
}
