package com.capstone.personalityTest.controller.test.testcontroller;

import com.capstone.personalityTest.dto.RequestDTO.test.TestRequest.QuestionRequest;
import com.capstone.personalityTest.dto.ResponseDTO.test.TestResponse.TestResponse;
import com.capstone.personalityTest.service.test.testservice.QuestionService;
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
public class QuestionController {

    private final QuestionService questionService;

    // 3. Add questions to a section
    @PreAuthorize("hasAnyRole('ORG_OWNER', 'DEVELOPER')")
    @PatchMapping("/{testId}/sections/{sectionId}/questions")
    public ResponseEntity<TestResponse> addQuestions(
            @PathVariable Long testId,
            @PathVariable Long sectionId,
            @Valid @RequestBody QuestionRequest question) {
        TestResponse updatedTest = questionService.addQuestions(testId, sectionId, question);
        return new ResponseEntity<>(updatedTest, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('ORG_OWNER', 'DEVELOPER')")
    @DeleteMapping("/{testId}/questions/{questionId}")
    public ResponseEntity<String> deleteQuestion(@PathVariable Long testId, @PathVariable Long questionId) {
        questionService.deleteQuestion(testId, questionId);
        return new ResponseEntity<>("Question deleted successfully", HttpStatus.OK);
    }

    // Update Question
    @PreAuthorize("hasAnyRole('ORG_OWNER', 'DEVELOPER')")
    @PatchMapping("/{testId}/questions/{questionId}")
    public ResponseEntity<TestResponse> updateQuestion(
            @PathVariable Long testId,
            @PathVariable Long questionId,
            @Valid @RequestBody QuestionRequest questionRequest) {
        TestResponse updatedTest = questionService.updateQuestion(testId, questionId, questionRequest);
        return ResponseEntity.ok(updatedTest);
    }
}
