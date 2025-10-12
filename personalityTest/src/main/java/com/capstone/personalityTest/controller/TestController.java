package com.capstone.personalityTest.controller;

import com.capstone.personalityTest.dto.RequestDTO.TestRequest.*;
import com.capstone.personalityTest.dto.ResponseDTO.TestResponse.TestResponse;
import com.capstone.personalityTest.service.JwtService;
import com.capstone.personalityTest.service.TestService;
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

    // 2. Add sections to a test
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{testId}/sections")
    public ResponseEntity<TestResponse> addSections(
            @PathVariable Long testId,
            @Valid @RequestBody SectionRequest section) {
        TestResponse updatedTest = testService.addSections(testId, section);

        return new ResponseEntity<>(updatedTest, HttpStatus.CREATED);
    }

    // 3. Add questions to a section
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{testId}/sections/{sectionId}/questions")
    public ResponseEntity<TestResponse> addQuestions(
            @PathVariable Long testId,
            @PathVariable Long sectionId,
            @Valid @RequestBody QuestionRequest question) {
        TestResponse updatedTest = testService.addQuestions(testId, sectionId, question);
        return new ResponseEntity<>(updatedTest, HttpStatus.CREATED);
    }

    // 4. Add subquestions to a question
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{testId}/questions/{questionId}/subquestions")
    public ResponseEntity<TestResponse> addSubQuestions(
            @PathVariable Long testId,
            @PathVariable Long questionId,
            @Valid @RequestBody SubQuestionRequest subQuestion) {
        TestResponse updatedTest = testService.addSubQuestions(testId, questionId, subQuestion);
        return new ResponseEntity<>(updatedTest, HttpStatus.CREATED);
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
            @RequestParam boolean active
    ) {
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
    @DeleteMapping("/{testId}/sections/{sectionId}")
    public ResponseEntity<String> deleteSection(@PathVariable Long testId, @PathVariable Long sectionId) {
        testService.deleteSection(testId, sectionId);
        return new ResponseEntity<>("Section deleted successfully", HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{testId}/questions/{questionId}")
    public ResponseEntity<String> deleteQuestion(@PathVariable Long testId, @PathVariable Long questionId) {
        testService.deleteQuestion(testId, questionId);
        return new ResponseEntity<>("Question deleted successfully", HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{testId}/subquestions/{subQuestionId}")
    public ResponseEntity<String> deleteSubQuestion(@PathVariable Long testId, @PathVariable Long subQuestionId) {
        testService.deleteSubQuestion(testId, subQuestionId);
        return new ResponseEntity<>("SubQuestion deleted successfully", HttpStatus.OK);
    }


    // --- UPDATE APIs ---

    // Update Section
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{testId}/sections/{sectionId}")
    public ResponseEntity<TestResponse> updateSection(
            @PathVariable Long testId,
            @PathVariable Long sectionId,
            @Valid @RequestBody SectionRequest sectionRequest) {
        TestResponse updatedTest = testService.updateSection(testId, sectionId, sectionRequest);
        return ResponseEntity.ok(updatedTest);
    }

    // Update Question
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{testId}/questions/{questionId}")
    public ResponseEntity<TestResponse> updateQuestion(
            @PathVariable Long testId,
            @PathVariable Long questionId,
            @Valid @RequestBody QuestionRequest questionRequest) {
        TestResponse updatedTest = testService.updateQuestion(testId, questionId, questionRequest);
        return ResponseEntity.ok(updatedTest);
    }

    // Update SubQuestion
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{testId}/subquestions/{subQuestionId}")
    public ResponseEntity<TestResponse> updateSubQuestion(
            @PathVariable Long testId,
            @PathVariable Long subQuestionId,
            @Valid @RequestBody SubQuestionRequest subQuestionRequest) {
        TestResponse updatedTest = testService.updateSubQuestion(testId, subQuestionId, subQuestionRequest);
        return ResponseEntity.ok(updatedTest);
    }


}

