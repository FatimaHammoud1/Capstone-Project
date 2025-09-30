package com.capstone.personalityTest.controller;

import com.capstone.personalityTest.dto.RequestDTO.TestRequest.*;
import com.capstone.personalityTest.dto.ResponseDTO.TestResponse.TestResponse;
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
            @Valid @RequestBody List<SectionRequest> sections) {
        TestResponse updatedTest = testService.addSections(testId, sections);

        return new ResponseEntity<>(updatedTest, HttpStatus.CREATED);
    }

    // 3. Add questions to a section
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{testId}/sections/{sectionId}/questions")
    public ResponseEntity<TestResponse> addQuestions(
            @PathVariable Long testId,
            @PathVariable Long sectionId,
            @Valid @RequestBody List<QuestionRequest> questions) {
        TestResponse updatedTest = testService.addQuestions(testId, sectionId, questions);
        return new ResponseEntity<>(updatedTest, HttpStatus.CREATED);
    }

    // 4. Add subquestions to a question
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{testId}/questions/{questionId}/subquestions")
    public ResponseEntity<TestResponse> addSubQuestions(
            @PathVariable Long testId,
            @PathVariable Long questionId,
            @Valid @RequestBody List<SubQuestionRequest> subQuestions) {
        TestResponse updatedTest = testService.addSubQuestions(testId, questionId, subQuestions);
        return new ResponseEntity<>(updatedTest, HttpStatus.CREATED);
    }

    // 5. Confirm test (finalize)
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{testId}/confirm")
    public ResponseEntity<TestResponse> confirmTest(@PathVariable Long testId) {
        TestResponse confirmedTest = testService.confirmTest(testId);
        return new ResponseEntity<>(confirmedTest, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}")
    public ResponseEntity<TestResponse> updateTest(
            @PathVariable Long id,
            @RequestBody UpdateTestRequest updateTestDto) {
        TestResponse updatedTest = testService.updateTest(id, updateTestDto);
        return ResponseEntity.ok(updatedTest);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<TestResponse>> getAllTests() {
        List<TestResponse> tests = testService.getAllTests();
        return ResponseEntity.ok(tests);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<TestResponse> getTestById(@PathVariable Long id) {
        TestResponse test = testService.getTestById(id);
        return ResponseEntity.ok(test);
    }

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


}

