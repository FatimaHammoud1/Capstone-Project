package com.capstone.personalityTest.mapper;


import com.capstone.personalityTest.dto.ResponseDTO.TestAttemptResponse.AnswerResponse;
import com.capstone.personalityTest.model.Enum.AnswerType;
import com.capstone.personalityTest.model.TestAttempt.Answer.Answer;
import com.capstone.personalityTest.model.TestAttempt.Answer.CheckBoxAnswer;
import com.capstone.personalityTest.model.TestAttempt.Answer.OpenAnswer;
import com.capstone.personalityTest.model.TestAttempt.Answer.ScaleAnswer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.AfterMapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AnswerMapper {

    @Mapping(source = "question.id", target = "questionId")
    @Mapping(source = "question.questionText", target = "questionText")
    @Mapping(source = "subQuestion.id", target = "subQuestionId")
    @Mapping(source = "subQuestion.subQuestionText", target = "subQuestionText")
    AnswerResponse toDto(Answer answer);

    @Mapping(source = "answers", target = "answers")
    List<AnswerResponse> toDtoList(List<Answer> answers);

    // This runs after MapStruct mapping to fill subclass-specific fields
    @AfterMapping
    default void fillSubclassFields(Answer answer, @MappingTarget AnswerResponse dto) {
        if (answer instanceof CheckBoxAnswer binary) {
            dto.setAnswerType(AnswerType.CHECKBOX);  // or BINARY if you prefer
            dto.setBinaryValue(binary.getBinaryValue());
        } else if (answer instanceof ScaleAnswer scale) {
            dto.setAnswerType(AnswerType.SCALE);
            dto.setScaleValue(scale.getScaleValue());
        } else if (answer instanceof OpenAnswer open) {
            dto.setAnswerType(AnswerType.OPEN);
            dto.setOpenValues(open.getValues());
        }
    }
}

