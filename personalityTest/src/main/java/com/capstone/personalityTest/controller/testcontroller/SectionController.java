package com.capstone.personalityTest.controller.testcontroller;

import com.capstone.personalityTest.dto.RequestDTO.TestRequest.SectionRequest;
import com.capstone.personalityTest.dto.ResponseDTO.TestResponse.TestResponse;
import com.capstone.personalityTest.service.testservice.SectionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping("/api/tests")
@RequiredArgsConstructor
public class SectionController {

    private final SectionService sectionService;

    // 2. Add sections to a test
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{testId}/sections")
    public ResponseEntity<TestResponse> addSections(
            @PathVariable Long testId,
            @Valid @RequestBody SectionRequest section) {
        TestResponse updatedTest = sectionService.addSections(testId, section);
        return new ResponseEntity<>(updatedTest, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{testId}/sections/{sectionId}")
    public ResponseEntity<String> deleteSection(@PathVariable Long testId, @PathVariable Long sectionId) {
        sectionService.deleteSection(testId, sectionId);
        return new ResponseEntity<>("Section deleted successfully", HttpStatus.OK);
    }

    // Update Section
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{testId}/sections/{sectionId}")
    public ResponseEntity<TestResponse> updateSection(
            @PathVariable Long testId,
            @PathVariable Long sectionId,
            @Valid @RequestBody SectionRequest sectionRequest) {
        TestResponse updatedTest = sectionService.updateSection(testId, sectionId, sectionRequest);
        return ResponseEntity.ok(updatedTest);
    }
}

