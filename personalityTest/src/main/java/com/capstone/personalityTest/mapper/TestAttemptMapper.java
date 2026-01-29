package com.capstone.personalityTest.mapper;

import com.capstone.personalityTest.dto.RequestDTO.test.TestAttemptRequest.TestAttemptRequest;
import com.capstone.personalityTest.dto.ResponseDTO.test.TestAttemptResponse.TestAttemptResponse;
import com.capstone.personalityTest.dto.ResponseDTO.test.TestAttemptResponse.TestAttemptWithAnswersResponse;
import com.capstone.personalityTest.model.testm.TestAttempt.TestAttempt;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring" , uses = {AnswerMapper.class})
public interface TestAttemptMapper {

    TestAttempt toEntity(TestAttemptRequest testDto);

    TestAttemptResponse toDto(TestAttempt test);

    @Mapping(source = "id", target = "attemptId")
    @Mapping(source = "test.id", target = "testId")
    @Mapping(source = "test.title", target = "testTitle")
    @Mapping(source = "student.id", target = "studentId")
    @Mapping(source = "student.name", target = "studentName")
    TestAttemptWithAnswersResponse toAdminDto(TestAttempt attempt);

    List<TestAttemptWithAnswersResponse> toAdminDtoList(List<TestAttempt> attempts);
}
