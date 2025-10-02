package com.capstone.personalityTest.service;

import com.capstone.personalityTest.dto.RequestDTO.TestRequest.*;
import com.capstone.personalityTest.dto.ResponseDTO.TestResponse.SubQuestionResponse;
import com.capstone.personalityTest.dto.ResponseDTO.TestResponse.TestResponse;
import com.capstone.personalityTest.mapper.TestMapper.QuestionMapper;
import com.capstone.personalityTest.mapper.TestMapper.SectionMapper;
import com.capstone.personalityTest.mapper.TestMapper.SubQuestionMapper;
import com.capstone.personalityTest.mapper.TestMapper.TestMapper;
import com.capstone.personalityTest.model.Enum.PersonalityTrait;
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
import java.util.Map;
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

        Test savedTest = testRepository.save(test);
        return testMapper.toDto(savedTest);
    }

    //  Add sections to test
    public TestResponse addSections(Long testId, SectionRequest sectionRequest) {
        Optional<Test> optionalTest = testRepository.findById(testId);

        if (optionalTest.isEmpty()) {
            throw new EntityNotFoundException("Test not found: " + testId);
        }

        Test test = optionalTest.get();

//        List<Section> sections = sectionRequests.stream()
//                .map(sectionMapper::toEntity)
//                .peek(section -> section.setTest(test)) // set parent
//                .collect(Collectors.toList());
        Section section = sectionMapper.toEntity(sectionRequest);
        section.setTest(test);

      //  sectionRepository.saveAll(sections);

        test.getSections().add(section);

        test.setStatus(TestStatus.DRAFT);

        testRepository.save(test);

        return testMapper.toDto(test);
    }

    //  Add questions to a section
    public TestResponse addQuestions(Long testId, Long sectionId, QuestionRequest questionRequest) {
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
//        List<Question> questions = questionRequests.stream()
//                .map(questionMapper::toEntity)
//                .peek(q -> q.setSection(section))
//                .collect(Collectors.toList());

     //   questionRepository.saveAll(questions);



        // Add questions to section

        Question question = questionMapper.toEntity(questionRequest);
        question.setSection(section);
        section.getQuestions().add(question);

        test.setStatus(TestStatus.DRAFT);

        //  Save test (cascades to sections/questions if cascade is configured)
        testRepository.save(test);

        //  Return response DTO with IDs
        return testMapper.toDto(test);
    }


    //  Add subQuestions to a question
    public TestResponse addSubQuestions(Long testId, Long questionId, SubQuestionRequest subQuestionRequest) {
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
//        List<SubQuestion> subQuestions = subQuestionRequests.stream()
//                .map(subQuestionMapper::toEntity)
//                .peek(sq -> sq.setQuestion(question))
//                .collect(Collectors.toList());

      //  subQuestionRepository.saveAll(subQuestions);

        // Add subquestions to the parent question
        SubQuestion subQuestion = subQuestionMapper.toEntity(subQuestionRequest);
        subQuestion.setQuestion(question);

        question.getSubQuestions().add(subQuestion);

        test.setStatus(TestStatus.DRAFT);

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
    @Transactional
    public TestResponse updateTest(Long id, TestRequest testRequest) {
        Test test = testRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Test not found with id " + id));

        testMapper.updateTestFromDto(testRequest, test); // MapStruct updates only non-null fields

        testRepository.save(test); // Persist changes

        return testMapper.toDto(test);
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


    // Update Section
    public TestResponse updateSection(Long testId, Long sectionId, SectionRequest sectionRequest) {
        Optional<Test> optionalTest = testRepository.findById(testId);
        if(optionalTest.isEmpty())
            throw new EntityNotFoundException("Test not found: " + testId);

        Test test = optionalTest.get();

        Optional<Section> optionalSection = sectionRepository.findById(sectionId);
        if(optionalSection.isEmpty()) throw  new EntityNotFoundException("Section not found: " + sectionId);

        Section section = optionalSection.get();

        if (!section.getTest().getId().equals(testId)) {
            throw new IllegalArgumentException("Section does not belong to test " + testId);
        }

        sectionMapper.updateSectionFromDto(sectionRequest, section);
        testRepository.save(test);
        return testMapper.toDto(test);
    }

    // Update Question
    public TestResponse updateQuestion(Long testId, Long questionId, QuestionRequest questionRequest) {
        Optional<Test> optionalTest = testRepository.findById(testId);
        if(optionalTest.isEmpty())
                throw new EntityNotFoundException("Test not found: " + testId);

        Test test = optionalTest.get();

        Optional<Question> optionalQuestion = questionRepository.findById(questionId);
        if(optionalQuestion.isEmpty()) throw new EntityNotFoundException("Question not found: " + questionId);

        Question question = optionalQuestion.get();

        questionMapper.updateQuestionFromDto(questionRequest, question);
        testRepository.save(test);
        return testMapper.toDto(test);
    }

    // Update SubQuestion
    public TestResponse updateSubQuestion(Long testId, Long subQuestionId, SubQuestionRequest subQuestionRequest) {
        Optional<Test> optionalTest = testRepository.findById(testId);
        if(optionalTest.isEmpty())
            throw new EntityNotFoundException("Test not found: " + testId);

        Test test = optionalTest.get();

        Optional<SubQuestion> optionalSubQuestion = subQuestionRepository.findById(subQuestionId);
        if(optionalSubQuestion.isEmpty()) throw new EntityNotFoundException("SubQuestion not found: " + subQuestionId);

        SubQuestion subQuestion = optionalSubQuestion.get();

        subQuestionMapper.updateSubQuestionFromDto(subQuestionRequest, subQuestion);
        testRepository.save(test);
        return testMapper.toDto(test);
    }

}

