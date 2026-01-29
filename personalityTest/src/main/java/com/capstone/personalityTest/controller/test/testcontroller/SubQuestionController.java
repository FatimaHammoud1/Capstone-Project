package com.capstone.personalityTest.controller.test.testcontroller;

import com.capstone.personalityTest.dto.RequestDTO.test.TestRequest.SubQuestionRequest;
import com.capstone.personalityTest.dto.ResponseDTO.test.TestResponse.TestResponse;
import com.capstone.personalityTest.service.test.testservice.SubQuestionService;
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
public class SubQuestionController {

    private final SubQuestionService subQuestionService;

    // 4. Add subquestions to a question
    @PreAuthorize("hasAnyRole('ORG_OWNER', 'DEVELOPER')")
    @PatchMapping("/{testId}/questions/{questionId}/subquestions")
    public ResponseEntity<TestResponse> addSubQuestions(
            @PathVariable Long testId,
            @PathVariable Long questionId,
            @Valid @RequestBody SubQuestionRequest subQuestion) {
        TestResponse updatedTest = subQuestionService.addSubQuestions(testId, questionId, subQuestion);
        return new ResponseEntity<>(updatedTest, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('ORG_OWNER', 'DEVELOPER')")
    @DeleteMapping("/{testId}/subquestions/{subQuestionId}")
    public ResponseEntity<String> deleteSubQuestion(@PathVariable Long testId, @PathVariable Long subQuestionId) {
        subQuestionService.deleteSubQuestion(testId, subQuestionId);
        return new ResponseEntity<>("SubQuestion deleted successfully", HttpStatus.OK);
    }

    // Update SubQuestion
    @PreAuthorize("hasAnyRole('ORG_OWNER', 'DEVELOPER')")
    @PatchMapping("/{testId}/subquestions/{subQuestionId}")
    public ResponseEntity<TestResponse> updateSubQuestion(
            @PathVariable Long testId,
            @PathVariable Long subQuestionId,
            @Valid @RequestBody SubQuestionRequest subQuestionRequest) {
        TestResponse updatedTest = subQuestionService.updateSubQuestion(testId, subQuestionId, subQuestionRequest);
        return ResponseEntity.ok(updatedTest);
    }
}
