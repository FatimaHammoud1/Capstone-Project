package com.capstone.personalityTest.mapper.TestMapper;

import com.capstone.personalityTest.dto.RequestDTO.TestRequest.TestRequest;
import com.capstone.personalityTest.dto.ResponseDTO.TestResponse.TestResponse;
import com.capstone.personalityTest.model.Test.Test;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring",uses = { SectionMapper.class, QuestionMapper.class, SubQuestionMapper.class })
public interface TestMapper {

    Test toEntity(TestRequest testDto);

    TestResponse toDto(Test test);

    @AfterMapping
    default void setParentReferences(TestRequest testDto, @MappingTarget Test test) {
        if (test.getSections() != null) {
            test.getSections().forEach(section -> {
                section.setTest(test);
                if (section.getQuestions() != null) {
                    section.getQuestions().forEach(question -> question.setSection(section));
                }
            });
        }
    }
}
