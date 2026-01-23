package com.capstone.personalityTest.controller.Exhibition;

import com.capstone.personalityTest.dto.ResponseDTO.Exhibition.SchoolResponse;
import com.capstone.personalityTest.service.Exhibition.SchoolService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api/schools")
@RequiredArgsConstructor
public class SchoolController {

    private final SchoolService schoolService;

    @GetMapping
    public ResponseEntity<List<SchoolResponse>> getAllSchools() {
        return ResponseEntity.ok(schoolService.getAllSchools());
    }

    @GetMapping("/{schoolId}")
    public ResponseEntity<SchoolResponse> getSchoolById(@PathVariable Long schoolId) {
        return ResponseEntity.ok(schoolService.getSchoolById(schoolId));
    }

    @GetMapping("/owner/{ownerId}")
    // @PreAuthorize("hasAnyRole('ORG_OWNER', 'DEVELOPER', 'SCHOOL_ADMIN')") // Uncomment if security is needed
    public ResponseEntity<List<SchoolResponse>> getAllSchoolsByOwnerId(@PathVariable Long ownerId) {
        return ResponseEntity.ok(schoolService.getSchoolsByOwnerId(ownerId));
    }
}
