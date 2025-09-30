package com.capstone.personalityTest.mapper.TestMapper;

import com.capstone.personalityTest.dto.RequestDTO.TestRequest.SubQuestionRequest;
import com.capstone.personalityTest.dto.ResponseDTO.TestResponse.SubQuestionResponse;
import com.capstone.personalityTest.model.Test.SubQuestion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface SubQuestionMapper {

    SubQuestionMapper INSTANCE = Mappers.getMapper(SubQuestionMapper.class);

    @Mapping(target = "question", ignore = true)
    SubQuestion toEntity(SubQuestionRequest subQuestionDto);


    SubQuestionResponse toDto(SubQuestion subQuestion);
}
