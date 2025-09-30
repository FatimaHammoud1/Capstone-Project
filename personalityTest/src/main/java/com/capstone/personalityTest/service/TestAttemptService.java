package com.capstone.personalityTest.service;

import com.capstone.personalityTest.dto.ResponseDTO.TestAttemptResponse;
import com.capstone.personalityTest.dto.ResponseDTO.TestResponse.QuestionResponse;
import com.capstone.personalityTest.dto.ResponseDTO.TestResponse.SectionResponse;
import com.capstone.personalityTest.dto.ResponseDTO.TestResponse.SubQuestionResponse;
import com.capstone.personalityTest.mapper.TestMapper.QuestionMapper;
import com.capstone.personalityTest.mapper.TestMapper.SectionMapper;
import com.capstone.personalityTest.mapper.TestMapper.SubQuestionMapper;
import com.capstone.personalityTest.model.Enum.PersonalityTrait;
import com.capstone.personalityTest.model.Enum.TargetGender;
import com.capstone.personalityTest.model.Test.Question;
import com.capstone.personalityTest.model.Test.SubQuestion;
import com.capstone.personalityTest.model.Test.Test;
import com.capstone.personalityTest.model.TestAttempt.TestAttempt;
import com.capstone.personalityTest.model.UserInfo;
import com.capstone.personalityTest.repository.TestAttemptRepository;
import com.capstone.personalityTest.repository.TestRepo.TestRepository;
import com.capstone.personalityTest.repository.UserInfoRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.mapstruct.control.MappingControl;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TestAttemptService {

    private final TestRepository testRepository;
    private final TestAttemptRepository testAttemptRepository;
    private final UserInfoRepository userInfoRepository;
    private final SectionMapper sectionMapper;
    private final QuestionMapper questionMapper;
    private final SubQuestionMapper subQuestionMapper;

    public TestAttemptResponse startTest(Long testId, Long studentId) {
        Optional<UserInfo> optionalStudent = userInfoRepository.findById(studentId);
        if(optionalStudent.isEmpty())
                throw new EntityNotFoundException("Student not found");

        Optional<Test> optionalTest = testRepository.findById(testId);
        if(optionalTest.isEmpty())
            throw new EntityNotFoundException("Test not found");

        UserInfo student = optionalStudent.get();
        Test test = optionalTest.get();

        // Create TestAttempt
        TestAttempt testAttempt = new TestAttempt();
        testAttempt.setStudent(student);
        testAttempt.setTest(test);

        testAttemptRepository.save(testAttempt);

        // Map to DTO with filtering
        List<SectionResponse> sectionsResponse = test.getSections().stream()
                .map(section -> {
                    SectionResponse sectionResponse = sectionMapper.toDto(section);

                    List<QuestionResponse> questionsResponse = section.getQuestions().stream()
                            .filter(q -> isQuestionVisible(q, student))
                            .map(q -> {
                                // filter subQuestions based on student gender
                                List<SubQuestion> filteredSubQuestions = q.getSubQuestions().stream()
                                        .filter(sq -> isSubQuestionVisible(sq, student))
                                        .toList();

                                // replace the original subQuestions with filtered ones
                                q.setSubQuestions(filteredSubQuestions);

                                // now map to DTO (mapper will group them)
                                return questionMapper.toDto(q);
                            }).toList();


                    sectionResponse.setQuestions(questionsResponse);
                    return sectionResponse;
                }).toList();

        TestAttemptResponse response = new TestAttemptResponse();
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

}
