package com.capstone.personalityTest.mapper.TestMapper;


import com.capstone.personalityTest.dto.RequestDTO.TestRequest.QuestionRequest;
import com.capstone.personalityTest.dto.ResponseDTO.TestResponse.QuestionResponse;
import com.capstone.personalityTest.model.Test.Question;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {SubQuestionMapper.class})
public interface QuestionMapper {

    Question toEntity(QuestionRequest questionDto);

    QuestionResponse toDto(Question question);
}
