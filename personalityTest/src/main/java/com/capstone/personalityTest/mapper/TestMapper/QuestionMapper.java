package com.capstone.personalityTest.mapper.TestMapper;


import com.capstone.personalityTest.dto.RequestDTO.test.TestRequest.QuestionRequest;
import com.capstone.personalityTest.dto.ResponseDTO.test.TestResponse.QuestionResponse;
import com.capstone.personalityTest.dto.ResponseDTO.test.TestResponse.SubQuestionResponse;
import com.capstone.personalityTest.model.testm.Test.Question;
import org.mapstruct.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {SubQuestionMapper.class}, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface QuestionMapper {

    Question toEntity(QuestionRequest questionDto);

    @Mapping(target = "groupedSubQuestions", ignore = true)
    QuestionResponse toDto(Question question);
    List<QuestionResponse> toDtoList(List<Question> questions);

    void updateQuestionFromDto(QuestionRequest request, @MappingTarget Question question);

    @AfterMapping
    default void groupSubQuestions(Question question, @MappingTarget QuestionResponse response) {
        if (question.getSubQuestions() != null && !question.getSubQuestions().isEmpty()) {
            Map<String, List<SubQuestionResponse>> grouped =
                    question.getSubQuestions().stream()
                            .map(subQuestion -> SubQuestionMapper.INSTANCE.toDto(subQuestion))
                            .filter(sq -> sq.getMetric() != null)
                            .collect(Collectors.groupingBy(
                                    sq -> sq.getMetric().getCode()
                            ));

            response.setGroupedSubQuestions(grouped);
        }
    }

}

