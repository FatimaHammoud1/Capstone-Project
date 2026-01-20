package com.capstone.personalityTest.service.test.testservice;

import com.capstone.personalityTest.dto.RequestDTO.test.TestRequest.QuestionRequest;
import com.capstone.personalityTest.dto.ResponseDTO.test.TestResponse.TestResponse;
import com.capstone.personalityTest.mapper.TestMapper.QuestionMapper;
import com.capstone.personalityTest.mapper.TestMapper.TestMapper;
import com.capstone.personalityTest.model.Enum.TestStatus;
import com.capstone.personalityTest.model.testm.Test.Question;
import com.capstone.personalityTest.model.testm.Test.Section;
import com.capstone.personalityTest.model.testm.Test.Test;
import com.capstone.personalityTest.repository.test.TestRepo.QuestionRepository;
import com.capstone.personalityTest.repository.test.TestRepo.SectionRepository;
import com.capstone.personalityTest.repository.test.TestRepo.TestRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final TestRepository testRepository;
    private final SectionRepository sectionRepository;
    private final QuestionRepository questionRepository;
    private final TestMapper testMapper;
    private final QuestionMapper questionMapper;

    //  Add questions to a section
    public TestResponse addQuestions(Long testId, Long sectionId, QuestionRequest questionRequest) {
        Optional<Test> optionalTest = testRepository.findById(testId);

        if (optionalTest.isEmpty()) {
            throw new EntityNotFoundException("Test not found: " + testId);
        }

        Test test = optionalTest.get();

        if (test.getStatus() == TestStatus.PUBLISHED) {
            throw new IllegalStateException("Cannot modify a published test");
        }

        Optional<Section> optionalSection = sectionRepository.findById(sectionId);
        if(optionalSection.isEmpty()){
                throw new EntityNotFoundException("Section not found: " + sectionId);
        }
        Section section= optionalSection.get();

        // Ensure the section belongs to the test
        if (!section.getTest().getId().equals(testId)) {
            throw new IllegalArgumentException("Section does not belong to the test with id: " + testId);
        }

        // Add questions to section
        Question question = questionMapper.toEntity(questionRequest);
        question.setSection(section);
        section.getQuestions().add(question);

        //  Save test (cascades to sections/questions if cascade is configured)
        testRepository.save(test);

        //  Return response DTO with IDs
        return testMapper.toDto(test);
    }

    @Transactional
    public void deleteQuestion(Long testId, Long questionId) {
        Optional<Question> optionalQuestion = questionRepository.findById(questionId);
        if (optionalQuestion.isEmpty()) {
            throw new EntityNotFoundException("Question not found with id " + questionId);
        }

        Question question = optionalQuestion.get();
        if (question.getSection().getTest().getStatus()==TestStatus.PUBLISHED){
            throw new IllegalStateException("Cannot delete a question in a published test");
        }

        if (!question.getSection().getTest().getId().equals(testId)) {
            throw new IllegalArgumentException("Question does not belong to test with id " + testId);
        }

        questionRepository.delete(question);
    }

    @Transactional
    // Update Question
    public TestResponse updateQuestion(Long testId, Long questionId, QuestionRequest questionRequest) {
        Optional<Test> optionalTest = testRepository.findById(testId);
        if(optionalTest.isEmpty())
                throw new EntityNotFoundException("Test not found: " + testId);

        Test test = optionalTest.get();
        if (test.getStatus() == TestStatus.PUBLISHED) {
            throw new IllegalStateException("Cannot modify a question in a test");
        }

        Optional<Question> optionalQuestion = questionRepository.findById(questionId);
        if(optionalQuestion.isEmpty()) throw new EntityNotFoundException("Question not found: " + questionId);

        Question question = optionalQuestion.get();

        questionMapper.updateQuestionFromDto(questionRequest, question);
        testRepository.save(test);
        return testMapper.toDto(test);
    }
}

