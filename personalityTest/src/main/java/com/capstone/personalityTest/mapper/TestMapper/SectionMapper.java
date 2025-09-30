package com.capstone.personalityTest.mapper.TestMapper;


import com.capstone.personalityTest.dto.RequestDTO.TestRequest.SectionRequest;
import com.capstone.personalityTest.dto.ResponseDTO.TestResponse.SectionResponse;
import com.capstone.personalityTest.model.Test.Section;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", uses = {QuestionMapper.class} ,nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SectionMapper {

    Section toEntity(SectionRequest sectionDto);

    SectionResponse toDto(Section section);

    void updateSectionFromDto(SectionRequest request, @MappingTarget Section section);
}
