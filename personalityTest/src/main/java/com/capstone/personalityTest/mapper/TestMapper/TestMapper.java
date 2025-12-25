package com.capstone.personalityTest.mapper.TestMapper;

import com.capstone.personalityTest.dto.RequestDTO.TestRequest.TestRequest;
import com.capstone.personalityTest.dto.ResponseDTO.TestResponse.TestResponse;
import com.capstone.personalityTest.model.Test.Test;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",uses = { SectionMapper.class, QuestionMapper.class, SubQuestionMapper.class } ,nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TestMapper {

    Test toEntity(TestRequest testDto);

    TestResponse toDto(Test test);

    void updateTestFromDto(TestRequest request, @MappingTarget Test test);

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
