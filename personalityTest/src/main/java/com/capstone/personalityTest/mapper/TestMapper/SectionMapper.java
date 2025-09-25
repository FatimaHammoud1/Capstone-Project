package com.capstone.personalityTest.mapper.TestMapper;


import com.capstone.personalityTest.dto.RequestDTO.TestRequest.SectionRequest;
import com.capstone.personalityTest.dto.ResponseDTO.TestResponse.SectionResponse;
import com.capstone.personalityTest.model.Test.Section;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {QuestionMapper.class})
public interface SectionMapper {

    Section toEntity(SectionRequest sectionDto);

    SectionResponse toDto(Section section);
}
