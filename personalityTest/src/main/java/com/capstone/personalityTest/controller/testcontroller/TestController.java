package com.capstone.personalityTest.controller.testcontroller;

import com.capstone.personalityTest.dto.RequestDTO.TestRequest.CreateVersionRequest;
import com.capstone.personalityTest.dto.RequestDTO.TestRequest.TestRequest;
import com.capstone.personalityTest.dto.ResponseDTO.TestResponse.TestResponse;
import com.capstone.personalityTest.service.JwtService;
import com.capstone.personalityTest.service.testservice.TestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api/tests")
@RequiredArgsConstructor
public class TestController {

    private final TestService testService;
    private final JwtService jwtService;

    // 1. Create a test (title + description only)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<TestResponse> createTest(@Valid @RequestBody TestRequest testRequest) {
        TestResponse createdTest = testService.createTest(testRequest);
        return new ResponseEntity<>(createdTest, HttpStatus.CREATED);
    }

    // 5. Confirm test (finalize)
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{testId}/publish")
    public ResponseEntity<TestResponse> publishTest(@PathVariable Long testId) {
        TestResponse confirmedTest = testService.publishTest(testId);
        return new ResponseEntity<>(confirmedTest, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{testId}/active")
    public ResponseEntity<TestResponse> setTestActive(
            @PathVariable Long testId,
            @RequestParam boolean active) {
        TestResponse response = testService.setTestActive(testId, active);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}")
    public ResponseEntity<TestResponse> updateTest(
            @PathVariable Long id,
            @RequestBody TestRequest updateTest) {
        TestResponse updatedTest = testService.updateTest(id, updateTest);
        return ResponseEntity.ok(updatedTest);
    }

    @GetMapping
    public List<TestResponse> getAllTests(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7); // remove "Bearer "
        String role = jwtService.extractRoles(token).get(0); // get the first role

        return testService.getAllTests(role);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<TestResponse> getTestById(@PathVariable Long id) {
        TestResponse test = testService.getTestById(id);
        return ResponseEntity.ok(test);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{testId}")
    public ResponseEntity<String> deleteTest(@PathVariable Long testId) {
        testService.deleteTest(testId);
        return new ResponseEntity<>("Test deleted successfully", HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/versions")
    public TestResponse createVersion(@RequestBody CreateVersionRequest request) {
        return testService.createVersion(request);
    }
}
