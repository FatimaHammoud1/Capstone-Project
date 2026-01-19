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

import com.capstone.personalityTest.dto.ResponseDTO.Exhibition.StudentRegistrationResponse;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentRegistrationController {

    private final StudentRegistrationService registrationService;

    // ----------------- REGISTER STUDENT -----------------
    @PostMapping("/register/{exhibitionId}")
    @PreAuthorize("hasAnyRole('STUDENT', 'DEVELOPER')")
    public ResponseEntity<StudentRegistrationResponse> registerStudent(
            @PathVariable Long exhibitionId,
            @AuthenticationPrincipal UserDetails userDetails) {

        StudentRegistrationResponse registration = registrationService.registerStudent(
                exhibitionId, userDetails.getUsername());
        return ResponseEntity.ok(registration);
    }

    // ----------------- LIST STUDENT REGISTRATIONS -----------------
    @GetMapping("/registrations")
    @PreAuthorize("hasAnyRole('STUDENT', 'DEVELOPER')")
    public ResponseEntity<List<StudentRegistrationResponse>> getRegistrations(
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(registrationService.getStudentRegistrations(userDetails.getUsername()));
    }

    // ----------------- Approve Single Student -----------------
    @PostMapping("/approve/{registrationId}")
    @PreAuthorize("hasAnyRole('ORG_OWNER', 'DEVELOPER')")
    public ResponseEntity<StudentRegistrationResponse> approveStudent(
            @PathVariable Long registrationId,
            @AuthenticationPrincipal UserDetails userDetails) {

        StudentRegistrationResponse approved = registrationService.approveStudent(registrationId, userDetails.getUsername());
        return ResponseEntity.ok(approved);
    }

    // ----------------- Approve Multiple Students -----------------
    @PostMapping("/approve-multiple")
    @PreAuthorize("hasAnyRole('ORG_OWNER', 'DEVELOPER')")
    public ResponseEntity<List<StudentRegistrationResponse>> approveStudents(
            @RequestBody List<Long> registrationIds,
            @AuthenticationPrincipal UserDetails userDetails) {

        List<StudentRegistrationResponse> approvedList = registrationService.approveStudents(registrationIds, userDetails.getUsername());
        return ResponseEntity.ok(approvedList);
    }
    
    // ----------------- CANCEL REGISTRATION -----------------
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('STUDENT', 'ORG_OWNER', 'DEVELOPER')")
    public ResponseEntity<StudentRegistrationResponse> cancelRegistration(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
            
        StudentRegistrationResponse cancelled = registrationService.cancelRegistration(id, userDetails.getUsername());
        return ResponseEntity.ok(cancelled);
    }
}
