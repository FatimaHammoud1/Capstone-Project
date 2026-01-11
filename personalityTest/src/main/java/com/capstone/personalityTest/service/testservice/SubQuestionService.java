package com.capstone.personalityTest.service.testservice;

import com.capstone.personalityTest.dto.RequestDTO.TestRequest.SubQuestionRequest;
import com.capstone.personalityTest.dto.ResponseDTO.TestResponse.TestResponse;
import com.capstone.personalityTest.mapper.TestMapper.SubQuestionMapper;
import com.capstone.personalityTest.mapper.TestMapper.TestMapper;
import com.capstone.personalityTest.model.Enum.TestStatus;
import com.capstone.personalityTest.model.Test.Metric;
import com.capstone.personalityTest.model.Test.Question;
import com.capstone.personalityTest.model.Test.SubQuestion;
import com.capstone.personalityTest.model.Test.Test;
import com.capstone.personalityTest.repository.TestRepo.MetricRepository;
import com.capstone.personalityTest.repository.TestRepo.QuestionRepository;
import com.capstone.personalityTest.repository.TestRepo.SubQuestionRepository;
import com.capstone.personalityTest.repository.TestRepo.TestRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SubQuestionService {

    private final TestRepository testRepository;
    private final QuestionRepository questionRepository;
    private final SubQuestionRepository subQuestionRepository;
    private final TestMapper testMapper;
    private final SubQuestionMapper subQuestionMapper;
    private final MetricRepository metricRepository;

    // Add subQuestions to a question
    public TestResponse addSubQuestions(Long testId, Long questionId, SubQuestionRequest subQuestionRequest) {
        // Check if test exists
        Optional<Test> optionalTest = testRepository.findById(testId);
        if (optionalTest.isEmpty()) {
            throw new EntityNotFoundException("Test not found: " + testId);
        }
        Test test = optionalTest.get();

        if (test.getStatus() == TestStatus.PUBLISHED) {
            throw new IllegalStateException("Cannot modify a published test");
        }

        // Fetch question by repository
        Optional<Question> optionalQuestion = questionRepository.findById(questionId);
        if (optionalQuestion.isEmpty()) {
            throw new EntityNotFoundException("Question not found: " + questionId);
        }
        Question question = optionalQuestion.get();

        // Ensure the question belongs to a section of the test
        if (!question.getSection().getTest().getId().equals(testId)) {
            throw new IllegalArgumentException("Question does not belong to the test with id: " + testId);
        }

        // Add subquestions to the parent question
        SubQuestion subQuestion = subQuestionMapper.toEntity(subQuestionRequest);
        subQuestion.setQuestion(question);

        question.getSubQuestions().add(subQuestion);

        // Save parent test (cascade will save everything if configured)
        testRepository.save(test);

        return testMapper.toDto(test);
    }

    @Transactional
    public void deleteSubQuestion(Long testId, Long subQuestionId) {
        Optional<SubQuestion> optionalSubQuestion = subQuestionRepository.findById(subQuestionId);
        if (optionalSubQuestion.isEmpty()) {
            throw new EntityNotFoundException("SubQuestion not found with id " + subQuestionId);
        }

        SubQuestion subQuestion = optionalSubQuestion.get();
        if (subQuestion.getQuestion().getSection().getTest().getStatus() == TestStatus.PUBLISHED) {
            throw new IllegalStateException("Cannot delete a published test");
        }

        if (!subQuestion.getQuestion().getSection().getTest().getId().equals(testId)) {
            throw new IllegalArgumentException("SubQuestion does not belong to test with id " + testId);
        }

        subQuestionRepository.delete(subQuestion);
    }

    @Transactional
    public TestResponse updateSubQuestion(
            Long testId,
            Long subQuestionId,
            SubQuestionRequest subQuestionRequest
    ) {

        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new EntityNotFoundException("Test not found"));

        if (test.getStatus() == TestStatus.PUBLISHED) {
            throw new IllegalStateException("Cannot modify a subquestion in a published test");
        }

        SubQuestion subQuestion = subQuestionRepository.findById(subQuestionId)
                .orElseThrow(() -> new EntityNotFoundException("SubQuestion not found"));

        //  Update simple fields
        subQuestionMapper.updateSubQuestionFromDto(subQuestionRequest, subQuestion);

        //  Update Metric SAFELY
        if (subQuestionRequest.getMetricId() != null) {
            Metric metric = metricRepository.findById(subQuestionRequest.getMetricId())
                    .orElseThrow(() -> new EntityNotFoundException("Metric not found"));

            subQuestion.setMetric(metric);
        }

        return testMapper.toDto(test);
    }

}
