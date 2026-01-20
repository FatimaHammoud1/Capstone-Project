package com.capstone.personalityTest.mapper.TestMapper;

import com.capstone.personalityTest.dto.RequestDTO.TestRequest.BaseTestRequest;
import com.capstone.personalityTest.model.Test.BaseTest;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",uses = { SectionMapper.class, QuestionMapper.class, SubQuestionMapper.class } ,nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface BaseTestMapper {

    BaseTest toEntity(BaseTestRequest baseTestRequest);

//    BaseTestResponse toDto(Test test);
}
