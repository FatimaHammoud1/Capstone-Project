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
    public ResponseEntity<com.capstone.personalityTest.dto.ResponseDTO.Exhibition.StudentRegistrationResponse> registerStudent(
            @PathVariable Long exhibitionId,
            @AuthenticationPrincipal UserDetails userDetails) {

        com.capstone.personalityTest.dto.ResponseDTO.Exhibition.StudentRegistrationResponse registration = registrationService.registerStudent(
                exhibitionId, userDetails.getUsername());
        return ResponseEntity.ok(registration);
    }

    // ----------------- LIST STUDENT REGISTRATIONS -----------------
    @GetMapping("/registrations")
    @PreAuthorize("hasAnyRole('STUDENT', 'DEVELOPER')")
    public ResponseEntity<List<com.capstone.personalityTest.dto.ResponseDTO.Exhibition.StudentRegistrationResponse>> getRegistrations(
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(registrationService.getStudentRegistrations(userDetails.getUsername()));
    }

    // ----------------- Approve Single Student -----------------
    @PostMapping("/approve/{registrationId}")
    @PreAuthorize("hasAnyRole('ORG_OWNER', 'DEVELOPER')")
    public ResponseEntity<com.capstone.personalityTest.dto.ResponseDTO.Exhibition.StudentRegistrationResponse> approveStudent(
            @PathVariable Long registrationId,
            @AuthenticationPrincipal UserDetails userDetails) {

        com.capstone.personalityTest.dto.ResponseDTO.Exhibition.StudentRegistrationResponse approved = registrationService.approveStudent(registrationId, userDetails.getUsername());
        return ResponseEntity.ok(approved);
    }

    // ----------------- Approve Multiple Students -----------------
    @PostMapping("/approve-multiple")
    @PreAuthorize("hasAnyRole('ORG_OWNER', 'DEVELOPER')")
    public ResponseEntity<List<com.capstone.personalityTest.dto.ResponseDTO.Exhibition.StudentRegistrationResponse>> approveStudents(
            @RequestBody List<Long> registrationIds,
            @AuthenticationPrincipal UserDetails userDetails) {

        List<com.capstone.personalityTest.dto.ResponseDTO.Exhibition.StudentRegistrationResponse> approvedList = registrationService.approveStudents(registrationIds, userDetails.getUsername());
        return ResponseEntity.ok(approvedList);
    }
    
    // ----------------- CANCEL REGISTRATION -----------------
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('STUDENT', 'ORG_OWNER', 'DEVELOPER')")
    public ResponseEntity<com.capstone.personalityTest.dto.ResponseDTO.Exhibition.StudentRegistrationResponse> cancelRegistration(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
            
        com.capstone.personalityTest.dto.ResponseDTO.Exhibition.StudentRegistrationResponse cancelled = registrationService.cancelRegistration(id, userDetails.getUsername());
        return ResponseEntity.ok(cancelled);
    }
}
