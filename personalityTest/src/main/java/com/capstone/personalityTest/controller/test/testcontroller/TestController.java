package com.capstone.personalityTest.controller.test.testcontroller;

import com.capstone.personalityTest.dto.RequestDTO.test.TestRequest.CreateVersionRequest;
import com.capstone.personalityTest.dto.RequestDTO.test.TestRequest.TestRequest;
import com.capstone.personalityTest.dto.ResponseDTO.test.TestResponse.TestResponse;

import com.capstone.personalityTest.service.test.testservice.TestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

@RestController
@CrossOrigin
@RequestMapping("/api/tests")
@RequiredArgsConstructor
public class TestController {

    private final TestService testService;


    // 1. Create a test (title + description only)
    @PreAuthorize("hasAnyRole('ORG_OWNER', 'DEVELOPER')")
    @PostMapping
    public ResponseEntity<TestResponse> createTest(@Valid @RequestBody TestRequest testRequest) {
        TestResponse createdTest = testService.createTest(testRequest);
        return new ResponseEntity<>(createdTest, HttpStatus.CREATED);
    }

    // 5. Confirm test (finalize)
    @PreAuthorize("hasAnyRole('ORG_OWNER', 'DEVELOPER')")
    @PutMapping("/{testId}/publish")
    public ResponseEntity<TestResponse> publishTest(@PathVariable Long testId) {
        TestResponse confirmedTest = testService.publishTest(testId);
        return new ResponseEntity<>(confirmedTest, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('ORG_OWNER', 'DEVELOPER')")
    @PutMapping("/{testId}/active")
    public ResponseEntity<TestResponse> setTestActive(
            @PathVariable Long testId,
            @RequestParam boolean active) {
        TestResponse response = testService.setTestActive(testId, active);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ORG_OWNER', 'DEVELOPER')")
    @PatchMapping("/{id}")
    public ResponseEntity<TestResponse> updateTest(
            @PathVariable Long id,
            @RequestBody TestRequest updateTest) {
        TestResponse updatedTest = testService.updateTest(id, updateTest);
        return ResponseEntity.ok(updatedTest);
    }

    @GetMapping

    public List<TestResponse> getAllTests(Authentication authentication) {
        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(r -> r.replace("ROLE_", ""))
                .filter(r -> r.equals("ORG_OWNER") || r.equals("DEVELOPER"))
                .findFirst()
                .orElse("STUDENT");

        return testService.getAllTests(role);
    }

    @PreAuthorize("hasAnyRole('ORG_OWNER', 'DEVELOPER')")
    @GetMapping("/{id}")
    public ResponseEntity<TestResponse> getTestById(@PathVariable Long id) {
        TestResponse test = testService.getTestById(id);
        return ResponseEntity.ok(test);
    }

    @PreAuthorize("hasAnyRole('ORG_OWNER', 'DEVELOPER')")
    @DeleteMapping("/{testId}")
    public ResponseEntity<String> deleteTest(@PathVariable Long testId) {
        testService.deleteTest(testId);
        return new ResponseEntity<>("Test deleted successfully", HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('ORG_OWNER', 'DEVELOPER')")
    @PostMapping("/versions")
    public TestResponse createVersion(@RequestBody CreateVersionRequest request) {
        return testService.createVersion(request);
    }
}
