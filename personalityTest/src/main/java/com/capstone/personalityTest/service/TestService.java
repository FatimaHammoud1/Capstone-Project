package com.capstone.personalityTest.service;

import com.capstone.personalityTest.dto.RequestDTO.TestRequest.*;
import com.capstone.personalityTest.dto.ResponseDTO.TestResponse.TestResponse;
import com.capstone.personalityTest.mapper.TestMapper.QuestionMapper;
import com.capstone.personalityTest.mapper.TestMapper.SectionMapper;
import com.capstone.personalityTest.mapper.TestMapper.SubQuestionMapper;
import com.capstone.personalityTest.mapper.TestMapper.TestMapper;
import com.capstone.personalityTest.model.Enum.TestStatus;
import com.capstone.personalityTest.model.Test.Question;
import com.capstone.personalityTest.model.Test.Section;
import com.capstone.personalityTest.model.Test.SubQuestion;
import com.capstone.personalityTest.model.Test.Test;
import com.capstone.personalityTest.repository.TestRepo.QuestionRepository;
import com.capstone.personalityTest.repository.TestRepo.SectionRepository;
import com.capstone.personalityTest.repository.TestRepo.SubQuestionRepository;
import com.capstone.personalityTest.repository.TestRepo.TestRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TestService {

    private final TestRepository testRepository;
    private final SectionRepository sectionRepository;
    private final QuestionRepository questionRepository;
    private final SubQuestionRepository subQuestionRepository;
    private final TestMapper testMapper;
    private final SectionMapper sectionMapper;
    private final QuestionMapper questionMapper;
    private final SubQuestionMapper subQuestionMapper;

    //  Create test (title + description only)
    public TestResponse createTest(TestRequest testRequest) {
        // Map DTO → Entity
        Test test = testMapper.toEntity(testRequest);

        // Remove nested sections/questions for initial creation
        test.setSections(new ArrayList<>());

        Test savedTest = testRepository.save(test);
        return testMapper.toDto(savedTest);
    }

    //  Add sections to test
    public TestResponse addSections(Long testId, List<SectionRequest> sectionRequests) {
        Optional<Test> optionalTest = testRepository.findById(testId);

        if (optionalTest.isEmpty()) {
            throw new EntityNotFoundException("Test not found: " + testId);
        }

        Test test = optionalTest.get();


        List<Section> sections = sectionRequests.stream()
                .map(sectionMapper::toEntity)
                .peek(section -> section.setTest(test)) // set parent
                .collect(Collectors.toList());

        test.getSections().addAll(sections);
        testRepository.save(test);

        return testMapper.toDto(test);
    }

    //  Add questions to a section
    public TestResponse addQuestions(Long testId, Long sectionId, List<QuestionRequest> questionRequests) {
        Optional<Test> optionalTest = testRepository.findById(testId);

        if (optionalTest.isEmpty()) {
            throw new EntityNotFoundException("Test not found: " + testId);
        }

        Test test = optionalTest.get();
        Optional<Section> optionalSection = sectionRepository.findById(sectionId);
        if(optionalSection.isEmpty()){
                throw new EntityNotFoundException("Section not found: " + sectionId);
        }
        Section section= optionalSection.get();

        // Ensure the section belongs to the test
        if (!section.getTest().getId().equals(testId)) {
            throw new IllegalArgumentException("Section does not belong to the test with id: " + testId);
        }

        // Map and set parent references
        List<Question> questions = questionRequests.stream()
                .map(questionMapper::toEntity)
                .peek(q -> q.setSection(section))
                .collect(Collectors.toList());

        // Add questions to section
        section.getQuestions().addAll(questions);

        //  Save test (cascades to sections/questions if cascade is configured)
        testRepository.save(test);

        //  Return response DTO with IDs
        return testMapper.toDto(test);
    }


    //  Add subQuestions to a question
    public TestResponse addSubQuestions(Long testId, Long questionId, List<SubQuestionRequest> subQuestionRequests) {
        // Check if test exists
        Optional<Test> optionalTest = testRepository.findById(testId);
        if (optionalTest.isEmpty()) {
            throw new EntityNotFoundException("Test not found: " + testId);
        }
        Test test = optionalTest.get();

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

        // Map and set parent references
        List<SubQuestion> subQuestions = subQuestionRequests.stream()
                .map(subQuestionMapper::toEntity)
                .peek(sq -> sq.setQuestion(question))
                .collect(Collectors.toList());

        // Add subquestions to the parent question
        question.getSubQuestions().addAll(subQuestions);

        // Save parent test (cascade will save everything if configured)
        testRepository.save(test);

        return testMapper.toDto(test);
    }

    //  Confirm test (finalize)
    public TestResponse confirmTest(Long testId) {
        Optional<Test> optionalTest = testRepository.findById(testId);
        if(optionalTest.isEmpty()){
                throw new EntityNotFoundException("Test not found: " + testId);
        }
        Test test = optionalTest.get();

        test.setStatus(TestStatus.PUBLISHED); // you should have a status enum: DRAFT / PUBLISHED
        testRepository.save(test);

        return testMapper.toDto(test);
    }


    // Get all tests
    public List<TestResponse> getAllTests() {
        List<Test> tests = testRepository.findAll();
        return tests.stream()
                .map(testMapper::toDto)
                .collect(Collectors.toList());
    }

    // Get test by ID
    public TestResponse getTestById(Long id) {
        Optional<Test> optionalTest = testRepository.findById(id);
        if (optionalTest.isEmpty() ) throw new EntityNotFoundException("Test not found: " + id);

        Test test = optionalTest.get();
        return testMapper.toDto(test);
    }

    //Update Title and description
    public TestResponse updateTest(Long id, UpdateTestRequest updateTestRequest) {
        Optional<Test> optionalTest = testRepository.findById(id);
        if (optionalTest.isEmpty())
            throw new EntityNotFoundException("Test not found with id " + id);

        Test test = optionalTest.get();

        if (updateTestRequest.getTitle() != null) {
            test.setTitle(updateTestRequest.getTitle());
        }
        if (updateTestRequest.getDescription() != null) {
            test.setDescription(updateTestRequest.getDescription());
        }

        Test updatedTest = testRepository.save(test);
        return testMapper.toDto(updatedTest);


    }

    @Transactional
    public void deleteTest(Long testId) {
        if (!testRepository.existsById(testId)) {
            throw new EntityNotFoundException("Test not found with id " + testId);
        }
        testRepository.deleteById(testId);
    }

    @Transactional
    public void deleteSection(Long testId, Long sectionId) {
        Optional<Section> optionalSection = sectionRepository.findById(sectionId);
        if (optionalSection.isEmpty()) throw new EntityNotFoundException("Section not found with id " + sectionId);
        Section section = optionalSection.get();

        if (!section.getTest().getId().equals(testId)) {
            throw new IllegalArgumentException("Section does not belong to test with id " + testId);
        }

        sectionRepository.delete(section);
    }

    @Transactional
    public void deleteQuestion(Long testId, Long questionId) {
        Optional<Question> optionalQuestion = questionRepository.findById(questionId);
        if (optionalQuestion.isEmpty()) {
            throw new EntityNotFoundException("Question not found with id " + questionId);
        }

        Question question = optionalQuestion.get();

        if (!question.getSection().getTest().getId().equals(testId)) {
            throw new IllegalArgumentException("Question does not belong to test with id " + testId);
        }

        questionRepository.delete(question);
    }

    @Transactional
    public void deleteSubQuestion(Long testId, Long subQuestionId) {
        Optional<SubQuestion> optionalSubQuestion = subQuestionRepository.findById(subQuestionId);
        if (optionalSubQuestion.isEmpty()) {
            throw new EntityNotFoundException("SubQuestion not found with id " + subQuestionId);
        }

        SubQuestion subQuestion = optionalSubQuestion.get();

        if (!subQuestion.getQuestion().getSection().getTest().getId().equals(testId)) {
            throw new IllegalArgumentException("SubQuestion does not belong to test with id " + testId);
        }

        subQuestionRepository.delete(subQuestion);
    }








}

