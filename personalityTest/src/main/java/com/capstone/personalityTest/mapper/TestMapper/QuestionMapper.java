package com.capstone.personalityTest.mapper.TestMapper;


import com.capstone.personalityTest.dto.RequestDTO.TestRequest.QuestionRequest;
import com.capstone.personalityTest.dto.ResponseDTO.TestResponse.QuestionResponse;
import com.capstone.personalityTest.dto.ResponseDTO.TestResponse.SubQuestionResponse;
import com.capstone.personalityTest.model.Enum.PersonalityTrait;
import com.capstone.personalityTest.model.Test.Question;
import com.capstone.personalityTest.model.Test.SubQuestion;
import org.mapstruct.*;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {SubQuestionMapper.class})
public interface QuestionMapper {

    Question toEntity(QuestionRequest questionDto);

    @Mapping(target = "groupedSubQuestions", ignore = true)
    QuestionResponse toDto(Question question);

    @AfterMapping
    default void groupSubQuestions(Question question, @MappingTarget QuestionResponse response) {
        if (question.getSubQuestions() != null && !question.getSubQuestions().isEmpty()) {
            Map<PersonalityTrait, List<SubQuestionResponse>> grouped =
                    question.getSubQuestions().stream()
                            .map(subQuestion -> SubQuestionMapper.INSTANCE.toDto(subQuestion))
                            .collect(Collectors.groupingBy(SubQuestionResponse::getPersonalityTrait));
            response.setGroupedSubQuestions(grouped);
        }
    }

}

