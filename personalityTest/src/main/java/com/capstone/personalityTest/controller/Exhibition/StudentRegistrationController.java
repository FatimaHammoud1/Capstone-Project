package com.capstone.personalityTest.controller.Exhibition;

import com.capstone.personalityTest.model.Exhibition.StudentRegistration;
import com.capstone.personalityTest.service.Exhibition.StudentRegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentRegistrationController {

    private final StudentRegistrationService registrationService;

    // ----------------- REGISTER STUDENT -----------------
    @PostMapping("/register/{exhibitionId}")
    @PreAuthorize("hasAnyRole('STUDENT', 'DEVELOPER')")
    public ResponseEntity<StudentRegistration> registerStudent(
            @PathVariable Long exhibitionId,
            @AuthenticationPrincipal UserDetails userDetails) {

        StudentRegistration registration = registrationService.registerStudent(
                exhibitionId, userDetails.getUsername());
        return ResponseEntity.ok(registration);
    }

    // ----------------- LIST STUDENT REGISTRATIONS -----------------
    @GetMapping("/registrations")
    @PreAuthorize("hasAnyRole('STUDENT', 'DEVELOPER')")
    public ResponseEntity<List<StudentRegistration>> getRegistrations(
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(registrationService.getStudentRegistrations(userDetails.getUsername()));
    }

    // ----------------- Approve Single Student -----------------
    @PostMapping("/approve/{registrationId}")
    @PreAuthorize("hasAnyRole('ORG_OWNER', 'DEVELOPER')")
    public ResponseEntity<StudentRegistration> approveStudent(
            @PathVariable Long registrationId,
            @AuthenticationPrincipal UserDetails userDetails) {

        StudentRegistration approved = registrationService.approveStudent(registrationId, userDetails.getUsername());
        return ResponseEntity.ok(approved);
    }

    // ----------------- Approve Multiple Students -----------------
    @PostMapping("/approve-multiple")
    @PreAuthorize("hasAnyRole('ORG_OWNER', 'DEVELOPER')")
    public ResponseEntity<List<StudentRegistration>> approveStudents(
            @RequestBody List<Long> registrationIds,
            @AuthenticationPrincipal UserDetails userDetails) {

        List<StudentRegistration> approvedList = registrationService.approveStudents(registrationIds, userDetails.getUsername());
        return ResponseEntity.ok(approvedList);
    }
    
    // ----------------- CANCEL REGISTRATION -----------------
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('STUDENT', 'ORG_OWNER', 'DEVELOPER')")
    public ResponseEntity<StudentRegistration> cancelRegistration(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
            
        StudentRegistration cancelled = registrationService.cancelRegistration(id, userDetails.getUsername());
        return ResponseEntity.ok(cancelled);
    }
}
