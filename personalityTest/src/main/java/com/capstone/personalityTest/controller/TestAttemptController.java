package com.capstone.personalityTest.controller;

import com.capstone.personalityTest.dto.RequestDTO.TestAttemptRequest.AnswerRequest;
import com.capstone.personalityTest.dto.ResponseDTO.TestAttemptResponse.AnswerResponse;
import com.capstone.personalityTest.dto.ResponseDTO.TestAttemptResponse.TestAttemptWithAnswersResponse;
import com.capstone.personalityTest.dto.ResponseDTO.TestAttemptResponse.TestAttemptResponse;
import com.capstone.personalityTest.model.PersonalityResult;
import com.capstone.personalityTest.service.TestAttemptService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api/test-attempts")
@RequiredArgsConstructor
public class TestAttemptController {

    private final TestAttemptService testAttemptService;

    @GetMapping("/{testId}")
    public ResponseEntity<TestAttemptResponse> startTest(
            @PathVariable Long testId,
            @RequestParam Long studentId) {
        TestAttemptResponse response = testAttemptService.startTest(testId, studentId);
        return ResponseEntity.ok(response);
    }

    //    Endpoint to submit answers
    @PatchMapping("/{attemptId}/answers")
    public ResponseEntity<String> submitAnswers(
            @PathVariable Long attemptId,
            @RequestBody AnswerRequest answer) {
        testAttemptService.submitAnswers(attemptId, answer);
        return ResponseEntity.ok("Answers submitted successfully");
    }

    @PatchMapping("/{attemptId}/finalize")
    public ResponseEntity<PersonalityResult> finalizeAttempt(@PathVariable Long attemptId) {
        PersonalityResult result = testAttemptService.finalizeAttempt(attemptId);
        return ResponseEntity.ok(result);
    }



    @PreAuthorize("hasRole('ADMIN')")
    // Get all test attempts (for admin)
    @GetMapping
    public ResponseEntity<List<TestAttemptWithAnswersResponse>> getAllTestAttempts() {
        List<TestAttemptWithAnswersResponse> attempts = testAttemptService.getAllTestAttempts();
        return ResponseEntity.ok(attempts);
    }

    @GetMapping("/students/{studentId}")
    public ResponseEntity<List<TestAttemptWithAnswersResponse>> getAttemptsByStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(testAttemptService.getAttemptsByStudent(studentId));
    }

    @GetMapping("/{attemptId}/answers")
    public ResponseEntity<List<AnswerResponse>> getAnswersByTestAttempt(@PathVariable Long attemptId) {
        List<AnswerResponse> answers = testAttemptService.getAnswersByTestAttempt(attemptId);
        return ResponseEntity.ok(answers);
    }

    @GetMapping("/fullAttempt/{attemptId}")
    public ResponseEntity<TestAttemptWithAnswersResponse> getTestAttemptWithAnswersById(@PathVariable Long attemptId) {
        TestAttemptWithAnswersResponse response = testAttemptService.getTestAttemptWithAnswersById(attemptId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/attempts/{attemptId}")
    public ResponseEntity<TestAttemptResponse> getTestAttemptById(@PathVariable Long attemptId) {
        TestAttemptResponse response = testAttemptService.getTestAttemptById(attemptId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/answers")
    public ResponseEntity<List<AnswerResponse>> getAllAnswers(){
        List<AnswerResponse> answers = testAttemptService.getAllAnswers();
        return ResponseEntity.ok(answers);
    }



}