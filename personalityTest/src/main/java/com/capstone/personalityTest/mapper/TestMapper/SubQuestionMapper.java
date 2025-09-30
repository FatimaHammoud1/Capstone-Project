package com.capstone.personalityTest.mapper.TestMapper;

import com.capstone.personalityTest.dto.RequestDTO.TestRequest.SubQuestionRequest;
import com.capstone.personalityTest.dto.ResponseDTO.TestResponse.SubQuestionResponse;
import com.capstone.personalityTest.model.Test.SubQuestion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring" , nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SubQuestionMapper {

    SubQuestionMapper INSTANCE = Mappers.getMapper(SubQuestionMapper.class);

    @Mapping(target = "question", ignore = true)
    SubQuestion toEntity(SubQuestionRequest subQuestionDto);

    SubQuestionResponse toDto(SubQuestion subQuestion);

    void updateSubQuestionFromDto(SubQuestionRequest request, @MappingTarget SubQuestion subQuestion);
}
