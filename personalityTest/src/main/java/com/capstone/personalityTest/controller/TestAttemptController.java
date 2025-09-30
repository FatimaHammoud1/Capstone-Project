package com.capstone.personalityTest.controller;

import com.capstone.personalityTest.dto.RequestDTO.AnswerRequest;
import com.capstone.personalityTest.dto.ResponseDTO.TestAttemptResponse;
import com.capstone.personalityTest.service.TestAttemptService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
         @RequestBody List<AnswerRequest> answers) {
     testAttemptService.submitAnswers(attemptId, answers);
     return ResponseEntity.ok("Answers submitted successfully");
 }

}

