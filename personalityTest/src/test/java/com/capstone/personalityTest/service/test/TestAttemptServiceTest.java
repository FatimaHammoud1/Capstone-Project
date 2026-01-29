package com.capstone.personalityTest.service.test;

import com.capstone.personalityTest.dto.RequestDTO.test.TestAttemptRequest.AnswerRequest;
import com.capstone.personalityTest.dto.ResponseDTO.test.TestAttemptResponse.TestAttemptResponse;
import com.capstone.personalityTest.mapper.AnswerMapper;
import com.capstone.personalityTest.mapper.TestAttemptMapper;
import com.capstone.personalityTest.mapper.TestMapper.QuestionMapper;
import com.capstone.personalityTest.mapper.TestMapper.SectionMapper;
import com.capstone.personalityTest.mapper.TestMapper.SubQuestionMapper;
import com.capstone.personalityTest.model.Enum.AnswerType;
import com.capstone.personalityTest.model.Enum.TargetGender;
import com.capstone.personalityTest.model.Enum.TestStatus;
import com.capstone.personalityTest.model.testm.Test.Question;
import com.capstone.personalityTest.model.testm.Test.Section;
import com.capstone.personalityTest.model.testm.Test.SubQuestion;
import com.capstone.personalityTest.model.testm.TestAttempt.Answer.Answer;
import com.capstone.personalityTest.model.testm.TestAttempt.Answer.ScaleAnswer;
import com.capstone.personalityTest.model.testm.TestAttempt.TestAttempt;
import com.capstone.personalityTest.model.UserInfo;
import com.capstone.personalityTest.repository.test.AnswerRepository;
import com.capstone.personalityTest.repository.test.TestAttemptRepository;
import com.capstone.personalityTest.repository.test.TestRepo.MetricRepository;
import com.capstone.personalityTest.repository.test.TestRepo.QuestionRepository;
import com.capstone.personalityTest.repository.test.TestRepo.SubQuestionRepository;
import com.capstone.personalityTest.repository.test.TestRepo.TestRepository;
import com.capstone.personalityTest.repository.UserInfoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TestAttemptService
 * Tests critical business logic: test initialization, gender-based question filtering, and answer submission
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TestAttemptService Unit Tests")
@Transactional
@Rollback
class TestAttemptServiceTest {

    @Mock
    private TestRepository testRepository;

    @Mock
    private TestAttemptRepository testAttemptRepository;

    @Mock
    private UserInfoRepository userInfoRepository;

    @Mock
    private SectionMapper sectionMapper;

    @Mock
    private QuestionMapper questionMapper;

    @Mock
    private SubQuestionMapper subQuestionMapper;

    @Mock
    private SubQuestionRepository subQuestionRepository;

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private AnswerRepository answerRepository;

    @Mock
    private TestAttemptMapper testAttemptMapper;

    @Mock
    private AnswerMapper answerMapper;

    @Mock
    private AIIntegrationService aiIntegrationService;

    @Mock
    private MetricRepository metricRepository;

    @InjectMocks
    private TestAttemptService testAttemptService;

    private UserInfo maleStudent;
    private UserInfo femaleStudent;
    private com.capstone.personalityTest.model.testm.Test.Test publishedTest;
    private TestAttempt testAttempt;
    private Question maleTargetQuestion;
    private Question allGenderQuestion;
    private SubQuestion maleTargetSubQuestion;

    @BeforeEach
    void setUp() {
        // Setup male student
        maleStudent = new UserInfo();
        maleStudent.setId(1L);
        maleStudent.setEmail("male@test.com");
        maleStudent.setName("Male Student");
        maleStudent.setGender(TargetGender.MALE);

        // Setup female student
        femaleStudent = new UserInfo();
        femaleStudent.setId(2L);
        femaleStudent.setEmail("female@test.com");
        femaleStudent.setName("Female Student");
        femaleStudent.setGender(TargetGender.FEMALE);

        // Setup published test
        publishedTest = new com.capstone.personalityTest.model.testm.Test.Test();
        publishedTest.setId(1L);
        publishedTest.setTitle("Career Assessment Test");
        publishedTest.setDescription("Test to assess career preferences");
        publishedTest.setStatus(TestStatus.PUBLISHED);
        publishedTest.setActive(true);

        // Setup sections
        Section section = new Section();
        section.setId(1L);
        section.setTitle("Career Preferences");
        section.setTest(publishedTest);

        // Setup male-targeted question
        maleTargetQuestion = new Question();
        maleTargetQuestion.setId(1L);
        maleTargetQuestion.setQuestionText("Male-specific question");
        maleTargetQuestion.setTargetGender(TargetGender.MALE);
        maleTargetQuestion.setAnswerType(AnswerType.SCALE);
        maleTargetQuestion.setSection(section);
        maleTargetQuestion.setSubQuestions(new ArrayList<>());

        // Setup all-gender question
        allGenderQuestion = new Question();
        allGenderQuestion.setId(2L);
        allGenderQuestion.setQuestionText("All-gender question");
        allGenderQuestion.setTargetGender(TargetGender.ALL);
        allGenderQuestion.setAnswerType(AnswerType.SCALE);
        allGenderQuestion.setSection(section);
        allGenderQuestion.setSubQuestions(new ArrayList<>());

        // Setup male-targeted sub-question
        maleTargetSubQuestion = new SubQuestion();
        maleTargetSubQuestion.setId(1L);
        maleTargetSubQuestion.setSubQuestionText("Male sub-question");
        maleTargetSubQuestion.setTargetGender(TargetGender.MALE);
        maleTargetSubQuestion.setQuestion(maleTargetQuestion);

        section.setQuestions(Arrays.asList(maleTargetQuestion, allGenderQuestion));
        publishedTest.setSections(Arrays.asList(section));

        // Setup test attempt
        testAttempt = new TestAttempt();
        testAttempt.setId(1L);
        testAttempt.setStudent(maleStudent);
        testAttempt.setTest(publishedTest);
        testAttempt.setFinalized(false);
        testAttempt.setAnswers(new ArrayList<>());
    }

    @Test
    @DisplayName("Should successfully start test for valid student and published test")
    void testStartTest_Success() {
        // Arrange
        when(userInfoRepository.findById(1L)).thenReturn(Optional.of(maleStudent));
        when(testRepository.findById(1L)).thenReturn(Optional.of(publishedTest));
        when(testAttemptRepository.save(any(TestAttempt.class))).thenReturn(testAttempt);
        when(sectionMapper.toDtoList(anyList())).thenReturn(new ArrayList<>());

        // Act
        TestAttemptResponse response = testAttemptService.startTest(1L, 1L);

        // Assert
        assertNotNull(response, "Response should not be null");
        assertNotNull(response.getTestTitle(), "Test title should be set");
        assertEquals("Career Assessment Test", response.getTestTitle());
        assertNotNull(response.getSections(), "Sections should not be null");

        // Verify repository interactions
        verify(userInfoRepository).findById(1L);
        verify(testRepository).findById(1L);
        verify(testAttemptRepository).save(any(TestAttempt.class));

        // Verify test attempt was created correctly
        ArgumentCaptor<TestAttempt> attemptCaptor = ArgumentCaptor.forClass(TestAttempt.class);
        verify(testAttemptRepository).save(attemptCaptor.capture());
        TestAttempt savedAttempt = attemptCaptor.getValue();

        assertEquals(maleStudent, savedAttempt.getStudent());
        assertEquals(publishedTest, savedAttempt.getTest());
    }

    @Test
    @DisplayName("Should throw exception when test not found")
    void testStartTest_TestNotFound() {
        // Arrange
        when(userInfoRepository.findById(1L)).thenReturn(Optional.of(maleStudent));
        when(testRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> testAttemptService.startTest(999L, 1L));

        assertEquals("Test not found", exception.getMessage());
        verify(testAttemptRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when test is not published")
    void testStartTest_TestNotPublished() {
        // Arrange
        publishedTest.setStatus(TestStatus.DRAFT);

        when(userInfoRepository.findById(1L)).thenReturn(Optional.of(maleStudent));
        when(testRepository.findById(1L)).thenReturn(Optional.of(publishedTest));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> testAttemptService.startTest(1L, 1L));

        assertTrue(exception.getMessage().contains("Test is not available for attempts"));
        verify(testAttemptRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should filter male-targeted questions for female student")
    void testIsQuestionVisible_FiltersMaleQuestions() {
        // Act
        boolean maleQuestionVisibleToMale = testAttemptService.isQuestionVisible(maleTargetQuestion, maleStudent);
        boolean maleQuestionVisibleToFemale = testAttemptService.isQuestionVisible(maleTargetQuestion, femaleStudent);
        boolean allGenderQuestionVisibleToFemale = testAttemptService.isQuestionVisible(allGenderQuestion, femaleStudent);

        // Assert
        assertTrue(maleQuestionVisibleToMale, "Male-targeted question should be visible to male student");
        assertFalse(maleQuestionVisibleToFemale, "Male-targeted question should NOT be visible to female student");
        assertTrue(allGenderQuestionVisibleToFemale, "All-gender question should be visible to all students");
    }

    @Test
    @DisplayName("Should filter gender-specific sub-questions correctly")
    void testIsSubQuestionVisible_FiltersCorrectly() {
        // Setup female-targeted sub-question
        SubQuestion femaleSubQuestion = new SubQuestion();
        femaleSubQuestion.setId(2L);
        femaleSubQuestion.setTargetGender(TargetGender.FEMALE);

        SubQuestion allGenderSubQuestion = new SubQuestion();
        allGenderSubQuestion.setId(3L);
        allGenderSubQuestion.setTargetGender(TargetGender.ALL);

        // Act
        boolean maleSubVisibleToMale = testAttemptService.isSubQuestionVisible(maleTargetSubQuestion, maleStudent);
        boolean maleSubVisibleToFemale = testAttemptService.isSubQuestionVisible(maleTargetSubQuestion, femaleStudent);
        boolean femaleSubVisibleToMale = testAttemptService.isSubQuestionVisible(femaleSubQuestion, maleStudent);
        boolean femaleSubVisibleToFemale = testAttemptService.isSubQuestionVisible(femaleSubQuestion, femaleStudent);
        boolean allSubVisibleToMale = testAttemptService.isSubQuestionVisible(allGenderSubQuestion, maleStudent);

        // Assert
        assertTrue(maleSubVisibleToMale, "Male sub-question should be visible to male");
        assertFalse(maleSubVisibleToFemale, "Male sub-question should NOT be visible to female");
        assertFalse(femaleSubVisibleToMale, "Female sub-question should NOT be visible to male");
        assertTrue(femaleSubVisibleToFemale, "Female sub-question should be visible to female");
        assertTrue(allSubVisibleToMale, "All-gender sub-question should be visible to all");
    }

    @Test
    @DisplayName("Should successfully submit new scale answer")
    void testSubmitAnswers_NewScaleAnswer_Success() {
        // Arrange
        AnswerRequest answerRequest = new AnswerRequest();
        answerRequest.setQuestionId(2L);
        answerRequest.setSubQuestionId(null);
        answerRequest.setAnswerType(AnswerType.SCALE);
        answerRequest.setScaleValue(4);

        when(testAttemptRepository.findById(1L)).thenReturn(Optional.of(testAttempt));
        when(questionRepository.findById(2L)).thenReturn(Optional.of(allGenderQuestion));
        when(answerRepository.findByAttemptAndQuestionAndSubQuestion(1L, 2L, null))
                .thenReturn(Optional.empty());
        when(answerRepository.save(any(Answer.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(testAttemptRepository.save(any(TestAttempt.class))).thenReturn(testAttempt);

        // Act
        testAttemptService.submitAnswers(1L, answerRequest);

        // Assert
        verify(testAttemptRepository).findById(1L);
        verify(questionRepository).findById(2L);
        verify(answerRepository).findByAttemptAndQuestionAndSubQuestion(1L, 2L, null);
        verify(answerRepository).save(any(Answer.class));
        verify(testAttemptRepository).save(any(TestAttempt.class));

        // Verify answer was added to attempt
        ArgumentCaptor<TestAttempt> attemptCaptor = ArgumentCaptor.forClass(TestAttempt.class);
        verify(testAttemptRepository).save(attemptCaptor.capture());
        TestAttempt savedAttempt = attemptCaptor.getValue();

        assertFalse(savedAttempt.getAnswers().isEmpty(), "Answer should be added to test attempt");
    }

    @Test
    @DisplayName("Should throw exception when submitting answer with wrong answer type")
    void testSubmitAnswers_WrongAnswerType_ThrowsException() {
        // Arrange
        AnswerRequest answerRequest = new AnswerRequest();
        answerRequest.setQuestionId(2L);
        answerRequest.setAnswerType(AnswerType.CHECKBOX); // Question expects SCALE
        answerRequest.setBinaryValue(true);

        when(testAttemptRepository.findById(1L)).thenReturn(Optional.of(testAttempt));
        when(questionRepository.findById(2L)).thenReturn(Optional.of(allGenderQuestion));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> testAttemptService.submitAnswers(1L, answerRequest));

        assertTrue(exception.getMessage().contains("Answer type mismatch"));
        verify(answerRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when submitting to finalized test attempt")
    void testSubmitAnswers_FinalizedAttempt_ThrowsException() {
        // Arrange
        testAttempt.setFinalized(true);

        AnswerRequest answerRequest = new AnswerRequest();
        answerRequest.setQuestionId(2L);
        answerRequest.setAnswerType(AnswerType.SCALE);
        answerRequest.setScaleValue(3);

        when(testAttemptRepository.findById(1L)).thenReturn(Optional.of(testAttempt));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> testAttemptService.submitAnswers(1L, answerRequest));

        assertEquals("Cannot submit answers: this test attempt is already finalized.", exception.getMessage());
        verify(answerRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should update existing answer when resubmitting")
    void testSubmitAnswers_UpdateExistingAnswer_Success() {
        // Arrange
        AnswerRequest answerRequest = new AnswerRequest();
        answerRequest.setQuestionId(2L);
        answerRequest.setAnswerType(AnswerType.SCALE);
        answerRequest.setScaleValue(5); // New value

        ScaleAnswer existingAnswer = new ScaleAnswer();
        existingAnswer.setId(1L);
        existingAnswer.setScaleValue(3); // Old value
        existingAnswer.setQuestion(allGenderQuestion);
        existingAnswer.setTestAttempt(testAttempt);

        when(testAttemptRepository.findById(1L)).thenReturn(Optional.of(testAttempt));
        when(questionRepository.findById(2L)).thenReturn(Optional.of(allGenderQuestion));
        when(answerRepository.findByAttemptAndQuestionAndSubQuestion(1L, 2L, null))
                .thenReturn(Optional.of(existingAnswer));
        when(answerRepository.save(any(Answer.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(testAttemptRepository.save(any(TestAttempt.class))).thenReturn(testAttempt);

        // Act
        testAttemptService.submitAnswers(1L, answerRequest);

        // Assert
        ArgumentCaptor<Answer> answerCaptor = ArgumentCaptor.forClass(Answer.class);
        verify(answerRepository).save(answerCaptor.capture());
        
        Answer savedAnswer = answerCaptor.getValue();
        assertTrue(savedAnswer instanceof ScaleAnswer);
        assertEquals(5, ((ScaleAnswer) savedAnswer).getScaleValue(), "Answer should be updated with new value");
    }

    @Test
    @DisplayName("Should throw exception when sub-question doesn't belong to question")
    void testSubmitAnswers_InvalidSubQuestion_ThrowsException() {
        // Arrange
        AnswerRequest answerRequest = new AnswerRequest();
        answerRequest.setQuestionId(2L);
        answerRequest.setSubQuestionId(999L); // Invalid sub-question
        answerRequest.setAnswerType(AnswerType.SCALE);
        answerRequest.setScaleValue(3);

        when(testAttemptRepository.findById(1L)).thenReturn(Optional.of(testAttempt));
        when(questionRepository.findById(2L)).thenReturn(Optional.of(allGenderQuestion));
        when(subQuestionRepository.findByIdAndQuestionId(999L, 2L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> testAttemptService.submitAnswers(1L, answerRequest));

        assertTrue(exception.getMessage().contains("SubQuestion") && exception.getMessage().contains("does not belong"));
        verify(answerRepository, never()).save(any());
    }
}
