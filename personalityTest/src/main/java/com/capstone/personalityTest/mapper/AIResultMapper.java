package com.capstone.personalityTest.mapper;

import com.capstone.personalityTest.dto.ResponseDTO.test.AIResultResponseDTO;
import com.capstone.personalityTest.model.testm.AIResult;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AIResultMapper {

    @Mapping(source = "testAttempt.id", target = "testAttemptId")
    AIResultResponseDTO toDto(AIResult aiResult);
}
