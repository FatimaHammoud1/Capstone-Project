package com.capstone.personalityTest.controller.Exhibition;

import com.capstone.personalityTest.model.Exhibition.StudentRegistration;
import com.capstone.personalityTest.service.Exhibition.StudentAttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/registrations")
@RequiredArgsConstructor
public class StudentAttendanceController {

    private final StudentAttendanceService attendanceService;

    // ----------------- Mark Single Student Attendance -----------------
    @PostMapping("/attendance/{registrationId}")
    @PreAuthorize("hasAnyRole('ORG_OWNER', 'DEVELOPER')")
    public ResponseEntity<com.capstone.personalityTest.dto.ResponseDTO.Exhibition.StudentRegistrationResponse> markAttendance(
            @PathVariable Long registrationId,
            @RequestParam boolean attended,
            @AuthenticationPrincipal UserDetails userDetails) {

        com.capstone.personalityTest.dto.ResponseDTO.Exhibition.StudentRegistrationResponse updated = attendanceService.markAttendance(registrationId, attended, userDetails.getUsername());
        return ResponseEntity.ok(updated);
    }

    // ----------------- Mark Multiple Students Attendance -----------------
    @PostMapping("/attendance-multiple")
    @PreAuthorize("hasAnyRole('ORG_OWNER', 'DEVELOPER')")
    public ResponseEntity<List<com.capstone.personalityTest.dto.ResponseDTO.Exhibition.StudentRegistrationResponse>> markMultipleAttendance(
            @RequestBody List<Long> registrationIds,
            @RequestParam boolean attended,
            @AuthenticationPrincipal UserDetails userDetails) {

        List<com.capstone.personalityTest.dto.ResponseDTO.Exhibition.StudentRegistrationResponse> updatedList = attendanceService.markAttendanceMultiple(registrationIds, attended, userDetails.getUsername());
        return ResponseEntity.ok(updatedList);
    }
}
