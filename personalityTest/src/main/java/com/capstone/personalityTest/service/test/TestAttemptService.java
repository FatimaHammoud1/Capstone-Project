package com.capstone.personalityTest.service.test;

import com.capstone.personalityTest.dto.RequestDTO.test.TestAttemptRequest.AnswerRequest;
import com.capstone.personalityTest.dto.ResponseDTO.test.TestAttemptResponse.AnswerResponse;
import com.capstone.personalityTest.dto.ResponseDTO.test.TestAttemptResponse.TestAttemptWithAnswersResponse;
import com.capstone.personalityTest.dto.ResponseDTO.test.TestAttemptResponse.TestAttemptResponse;
import com.capstone.personalityTest.dto.ResponseDTO.test.TestResponse.SectionResponse;
import com.capstone.personalityTest.mapper.AnswerMapper;
import com.capstone.personalityTest.mapper.TestAttemptMapper;
import com.capstone.personalityTest.mapper.TestMapper.QuestionMapper;
import com.capstone.personalityTest.mapper.TestMapper.SectionMapper;
import com.capstone.personalityTest.mapper.TestMapper.SubQuestionMapper;
import com.capstone.personalityTest.model.Enum.AnswerType;
import com.capstone.personalityTest.model.Enum.TargetGender;
import com.capstone.personalityTest.model.Enum.TestStatus;
import com.capstone.personalityTest.model.testm.EvaluationResult;
import com.capstone.personalityTest.model.testm.Test.Metric;
import com.capstone.personalityTest.model.testm.Test.Question;
import com.capstone.personalityTest.model.testm.Test.Section;
import com.capstone.personalityTest.model.testm.Test.SubQuestion;
import com.capstone.personalityTest.model.testm.Test.Test;
import com.capstone.personalityTest.model.testm.TestAttempt.Answer.Answer;
import com.capstone.personalityTest.model.testm.TestAttempt.Answer.CheckBoxAnswer;
import com.capstone.personalityTest.model.testm.TestAttempt.Answer.OpenAnswer;
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
import com.capstone.personalityTest.service.test.AIIntegrationService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class TestAttemptService {

    private final TestRepository testRepository;
    private final TestAttemptRepository testAttemptRepository;
    private final UserInfoRepository userInfoRepository;
    private final SectionMapper sectionMapper;
    private final QuestionMapper questionMapper;
    private final SubQuestionMapper subQuestionMapper;
    private final SubQuestionRepository subQuestionRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final TestAttemptMapper testAttemptMapper;
    private final AnswerMapper answerMapper;
    private final AIIntegrationService aiIntegrationService;
    
    private final MetricRepository metricRepository;


    public TestAttemptResponse startTest(Long testId, Long studentId) {
        Optional<UserInfo> optionalStudent = userInfoRepository.findById(studentId);
        if(optionalStudent.isEmpty())
                throw new EntityNotFoundException("Student not found");

        Optional<Test> optionalTest = testRepository.findById(testId);
        if(optionalTest.isEmpty())
            throw new EntityNotFoundException("Test not found");

        UserInfo student = optionalStudent.get();
        Test test = optionalTest.get();

        if (test.getStatus() != TestStatus.PUBLISHED || !test.isActive()) {
            throw new IllegalStateException("Test is not available for attempts");
        }

        // Create TestAttempt
        TestAttempt testAttempt = new TestAttempt();
        testAttempt.setStudent(student);
        testAttempt.setTest(test);

        testAttemptRepository.save(testAttempt);

        //Pre-filter questions & sub questions
        List<Section> filteredSections = test.getSections().stream()
                .peek(section -> {
                    List<Question> filteredQuestions = section.getQuestions().stream()
                            .filter(q -> isQuestionVisible(q, student))
                            .peek(q -> q.setSubQuestions(
                                    q.getSubQuestions().stream()
                                            .filter(sq -> isSubQuestionVisible(sq, student))
                                            .toList()
                            )).toList();

                    section.setQuestions(filteredQuestions);
                }).toList();

       // MapStruct handles mapping
        List<SectionResponse> sectionsResponse = sectionMapper.toDtoList(filteredSections);

        TestAttemptResponse response = new TestAttemptResponse();
        response.setId(testAttempt.getId());
        response.setTestId(test.getId());
        response.setTestTitle(test.getTitle());
        response.setTestDescription(test.getDescription());
        response.setSections(sectionsResponse);

        return response;
    }

    public boolean isQuestionVisible(Question question, UserInfo student) {
        return question.getTargetGender() == TargetGender.ALL
                || question.getTargetGender() == student.getGender();
    }

    public boolean isSubQuestionVisible(SubQuestion subQuestion, UserInfo student) {
        return subQuestion.getTargetGender() == TargetGender.ALL
                || subQuestion.getTargetGender() == student.getGender();
    }


    @Transactional
    public void submitAnswers(Long attemptId, AnswerRequest answers) {
        Optional<TestAttempt> optionalTestAttempt = testAttemptRepository.findById(attemptId);
        if(optionalTestAttempt.isEmpty()) throw new EntityNotFoundException("TestAttempt not found");
        TestAttempt attempt = optionalTestAttempt.get();

        if (attempt.isFinalized()) {
            throw new IllegalStateException("Cannot submit answers: this test attempt is already finalized.");
        }



        Question question = questionRepository.findById(answers.getQuestionId())
                .orElseThrow(() -> new EntityNotFoundException("Question not found"));


        SubQuestion subQuestion = null;
        if (answers.getSubQuestionId() != null) {
            // Validate relationship in single query
            subQuestion = subQuestionRepository.findByIdAndQuestionId(
                            answers.getSubQuestionId(),
                            answers.getQuestionId()
                    )
                    .orElseThrow(() -> new IllegalArgumentException(
                            String.format(
                                    "SubQuestion %d does not belong to Question %d or does not exist",
                                    answers.getSubQuestionId(),
                                    answers.getQuestionId()
                            )
                    ));
        }

        // Validate answer type
        if (!question.getAnswerType().equals(answers.getAnswerType())) {
            throw new IllegalArgumentException(
                    String.format(
                            "Answer type mismatch: Question %d expects %s but got %s",
                            question.getId(),
                            question.getAnswerType(),
                            answers.getAnswerType()
                    )
            );
        }

        Optional<Answer> existing = answerRepository.findByAttemptAndQuestionAndSubQuestion(
                attemptId, answers.getQuestionId(), answers.getSubQuestionId());

        Answer answer;
        if (existing.isPresent()) {
                answer = existing.get(); // update existing
                if (answer instanceof OpenAnswer && answers.getOpenValues() != null) {
                    ((OpenAnswer) answer).setValues(new ArrayList<>(answers.getOpenValues()));
                } else if (answer instanceof CheckBoxAnswer && answers.getBinaryValue() != null) {
                    ((CheckBoxAnswer) answer).setBinaryValue(answers.getBinaryValue());
                } else if (answer instanceof ScaleAnswer && answers.getScaleValue() != null) {
                    ((ScaleAnswer) answer).setScaleValue(answers.getScaleValue());
                }
        } else {
                answer = getAnswer(answers); // create new
        }

            // Set common fields
            answer.setQuestion(question);
            answer.setSubQuestion(subQuestion);
            answer.setTestAttempt(attempt);

            // Add to attempt if new
            if (existing.isEmpty()) {
                attempt.getAnswers().add(answer);
            }

            answerRepository.save(answer);


        testAttemptRepository.save(attempt); // persist with result

    }

    @Transactional
    public EvaluationResult finalizeAttempt(Long attemptId) {
        TestAttempt attempt = testAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new EntityNotFoundException("TestAttempt not found"));

        UserInfo student = attempt.getStudent();
        Test test = attempt.getTest();

        if (attempt.isFinalized()) {
            throw new IllegalStateException("Test attempt already finalized.");
        }

        //make sure all questions are being answered
        int totalVisibleItems = test.getSections().stream()
                .flatMap(section -> section.getQuestions().stream())
                .filter(q -> isQuestionVisible(q, student)) // only visible questions
                .mapToInt(q -> {
                    // If it has visible subquestions → count them individually
                    long visibleSubCount = q.getSubQuestions().stream()
                            .filter(sq -> isSubQuestionVisible(sq, student))
                            .count();
                    return visibleSubCount > 0 ? (int) visibleSubCount : 1; // if no subs, count the question itself
                })
                .sum();

        // Count answered items
        long answeredItems = attempt.getAnswers().stream()
                .filter(a -> {
                    if (a instanceof CheckBoxAnswer cb) return cb.getBinaryValue() != null;
                    if (a instanceof ScaleAnswer sa) return sa.getScaleValue() != null;
                    if (a instanceof OpenAnswer oa) return oa.getValues() != null && !oa.getValues().isEmpty();
                    return false;
                })
                .map(a -> a.getSubQuestion() != null
                        ? "S" + a.getSubQuestion().getId()
                        : "Q" + a.getQuestion().getId())
                .distinct()
                .count();


        if (answeredItems < totalVisibleItems) {
            throw new IllegalStateException(
                    String.format("Cannot finalize: only %d/%d visible questions answered.",
                            answeredItems, totalVisibleItems)
            );
        }



        // Calculate final result (personality code + metric scores)
        EvaluationResult result = calculateResult(attempt.getAnswers(), test);
        attempt.setEvaluationResult(result);
        attempt.setFinalized(true); // Lock the test attempt

        // Save to database
        testAttemptRepository.save(attempt);
        
        // Note: AI analysis is now triggered manually via separate endpoint
        // See: POST /api/test-attempts/{attemptId}/analyze
        
        return result;
    }

    //helper function for submitAnswer to calculate the results
    private EvaluationResult calculateResult(List<Answer> answers, Test test) {

        Map<String, Integer> scores = new HashMap<>();

        // Initialize all metrics with 0 from the BaseTest
        if (test.getBaseTest() != null) {
            List<Metric> metrics = metricRepository.findByBaseTestId(test.getBaseTest().getId());
            for (Metric metric : metrics) {
                scores.put(metric.getCode(), 0);
            }
        }

        for (Answer answer : answers) {

            if (answer.getSubQuestion() == null) continue;

            String metricCode = answer.getSubQuestion()
                    .getMetric()
                    .getCode();

            if (metricCode == null) continue;

            // CHECKBOX → +1
            if (answer instanceof CheckBoxAnswer cb &&
                    Boolean.TRUE.equals(cb.getBinaryValue())) {

                scores.merge(metricCode, 1, Integer::sum);
            }

            // SCALE → +scaleValue
            else if (answer instanceof ScaleAnswer sa &&
                    sa.getScaleValue() != null) {

                scores.merge(metricCode, sa.getScaleValue(), Integer::sum);
            }

            // OPEN answers → ignored
        }

        EvaluationResult result = new EvaluationResult();
        result.setMetricScores(scores);
        result.calculateTopMetrics(); // renamed method

        return result;
    }



    private Answer getAnswer(AnswerRequest req) {
        Answer answer;

        switch (req.getAnswerType()) {

            case AnswerType.CHECKBOX:
                CheckBoxAnswer checkBoxAnswer = new CheckBoxAnswer();
                checkBoxAnswer.setBinaryValue(req.getBinaryValue());
                answer = checkBoxAnswer;
                break;

            case AnswerType.SCALE:
                ScaleAnswer scaleAnswer = new ScaleAnswer();
                scaleAnswer.setScaleValue(req.getScaleValue());
                answer = scaleAnswer;
                break;

            case AnswerType.OPEN:
                OpenAnswer openAnswer = new OpenAnswer();
                openAnswer.setValues(req.getOpenValues());
                answer = openAnswer;
                break;

            default:
                throw new IllegalArgumentException("Invalid answer type: " + req.getAnswerType());
        }

        return answer;
    }


    @Transactional
    public List<TestAttemptWithAnswersResponse> getAllTestAttempts() {
        return testAttemptMapper.toAdminDtoList(testAttemptRepository.findAll());
    }

    @Transactional
    public List<TestAttemptWithAnswersResponse> getAttemptsByStudent(Long studentId) {
        List<TestAttempt> attempts = testAttemptRepository.findByStudentId(studentId);
        return testAttemptMapper.toAdminDtoList(attempts);
    }

    @Transactional
    public List<AnswerResponse> getAnswersByTestAttempt(Long attemptId) {
        TestAttempt attempt = testAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new EntityNotFoundException("TestAttempt not found"));

        List<Answer> answers = answerRepository.findByTestAttempt(attempt);

        // Map to DTOs
        return answerMapper.toDtoList(answers);
    }


    public TestAttemptWithAnswersResponse getTestAttemptWithAnswersById(Long attemptId) {
        Optional<TestAttempt> testAttemptOptional = testAttemptRepository.findById(attemptId);
        if (testAttemptOptional.isEmpty())
            throw new EntityNotFoundException("TestAttempt not found with id " + attemptId);
        TestAttempt testAttempt = testAttemptOptional.get();
        return testAttemptMapper.toAdminDto(testAttempt);

    }

    public TestAttemptResponse getTestAttemptById(Long attemptId) {
        Optional<TestAttempt> testAttemptOptional = testAttemptRepository.findById(attemptId);
        if (testAttemptOptional.isEmpty())
            throw new EntityNotFoundException("TestAttempt not found with id " + attemptId);
        TestAttempt testAttempt = testAttemptOptional.get();
        Test test = testAttempt.getTest();

        TestAttemptResponse response = new TestAttemptResponse();
        response.setId(attemptId);
        response.setTestId(test.getId());
        response.setTestTitle(test.getTitle());
        response.setTestDescription(test.getDescription());
        response.setSections(sectionMapper.toDtoList(test.getSections()));

        return response;
    }

    public List<AnswerResponse> getAllAnswersByTestAttemptId(Long testAttemptId) {
        List<Answer> answers = answerRepository.findByTestAttemptId(testAttemptId);
        return answerMapper.toDtoList(answers);
    }

    /**
     * Manually trigger AI analysis for a finalized test attempt.
     * This method should be called AFTER the test has been finalized.
     * 
     * Validates:
     * - Test attempt exists
     * - Test attempt is finalized
     * - AI analysis hasn't already been run
     * 
     * Then triggers async AI analysis via AIIntegrationService.
     * 
     * @param attemptId ID of the finalized test attempt
     * @throws IllegalStateException if test is not finalized or AI already run
     */
    @Transactional
    public void triggerAIAnalysis(Long attemptId) {
        // Fetch test attempt
        TestAttempt attempt = testAttemptRepository.findById(attemptId)
            .orElseThrow(() -> new EntityNotFoundException("Test attempt not found: " + attemptId));
        
        // Validate test is finalized
        if (!attempt.isFinalized()) {
            throw new IllegalStateException(
                "Cannot trigger AI analysis: Test attempt must be finalized first. " +
                "Please call PATCH /api/test-attempts/" + attemptId + "/finalize first."
            );
        }
        
        // Check if AI analysis already exists
//        if (aiIntegrationService.getAIResultByAttemptId(attemptId) != null) {
//            throw new IllegalStateException(
//                "AI analysis already exists for this test attempt. " +
//                "Results can be retrieved at GET /api/ai-results/attempt/" + attemptId
//            );
//        }
        
        // Trigger AI analysis asynchronously
        // Results saved to ai_results table
        aiIntegrationService.triggerCompleteAIAnalysis(attempt.getId());
    }

}
