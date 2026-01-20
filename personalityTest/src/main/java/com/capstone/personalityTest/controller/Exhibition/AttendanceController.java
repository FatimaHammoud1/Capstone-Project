package com.capstone.personalityTest.controller.Exhibition;

import com.capstone.personalityTest.model.Exhibition.StudentRegistration;
import com.capstone.personalityTest.service.Exhibition.AttendanceService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

import com.capstone.personalityTest.dto.ResponseDTO.Exhibition.StudentRegistrationResponse;

@RestController
@RequestMapping("/api/registrations")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    // ----------------- Mark Single Student Attendance -----------------
    @PostMapping("/attendance/{registrationId}")
    @PreAuthorize("hasAnyRole('ORG_OWNER', 'DEVELOPER')")
    public ResponseEntity<StudentRegistrationResponse> markAttendance(
            @PathVariable Long registrationId,
            @RequestParam boolean attended,
            @AuthenticationPrincipal UserDetails userDetails) {

        StudentRegistrationResponse updated = attendanceService.markAttendance(registrationId, attended, userDetails.getUsername());
        return ResponseEntity.ok(updated);
    }

    // ----------------- Mark Multiple Students Attendance -----------------
    @PostMapping("/attendance-multiple")
    @PreAuthorize("hasAnyRole('ORG_OWNER', 'DEVELOPER')")
    public ResponseEntity<List<StudentRegistrationResponse>> markMultipleAttendance(
            @RequestBody List<Long> registrationIds,
            @RequestParam boolean attended,
            @AuthenticationPrincipal UserDetails userDetails) {

        List<StudentRegistrationResponse> updatedList = attendanceService.markAttendanceMultiple(registrationIds, attended, userDetails.getUsername());
        return ResponseEntity.ok(updatedList);
    }
    
    // ----------------- Mark University Attendance -----------------
    @PostMapping("/university/{participationId}/attend")
    @PreAuthorize("hasAnyRole('ORG_OWNER', 'DEVELOPER')")
    public ResponseEntity<Void> markUniversityAttendance(
            @PathVariable Long participationId,
            @AuthenticationPrincipal UserDetails userDetails) {

        attendanceService.markUniversityAttendance(participationId, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }
    
    // ----------------- Mark School Attendance -----------------
    @PostMapping("/school/{participationId}/attend")
    @PreAuthorize("hasAnyRole('ORG_OWNER', 'DEVELOPER')")
    public ResponseEntity<Void> markSchoolAttendance(
            @PathVariable Long participationId,
            @AuthenticationPrincipal UserDetails userDetails) {

        attendanceService.markSchoolAttendance(participationId, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }
    
    // ----------------- Mark Provider Attendance -----------------
    @PostMapping("/provider/{requestId}/attend")
    @PreAuthorize("hasAnyRole('ORG_OWNER', 'DEVELOPER')")
    public ResponseEntity<Void> markProviderAttendance(
            @PathVariable Long requestId,
            @AuthenticationPrincipal UserDetails userDetails) {

        attendanceService.markProviderAttendance(requestId, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }
}
