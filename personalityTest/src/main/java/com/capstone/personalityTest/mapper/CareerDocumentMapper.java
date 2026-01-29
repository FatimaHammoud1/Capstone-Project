package com.capstone.personalityTest.mapper;

import com.capstone.personalityTest.dto.ResponseDTO.CareerDocumentResponse.CareerDocumentResponse;
import com.capstone.personalityTest.model.testm.CareerDocument;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

/**
 * MapStruct mapper for CareerDocument entity.
 * Converts between entity and DTOs.
 */
@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface CareerDocumentMapper {

    /**
     * Convert CareerDocument entity to response DTO.
     * Maps baseTest.id to baseTestId and baseTest.code to baseTestCode.
     */
    @Mapping(source = "baseTest.id", target = "baseTestId")
    @Mapping(source = "baseTest.code", target = "baseTestCode")
    CareerDocumentResponse toDto(CareerDocument document);

    /**
     * Convert list of CareerDocument entities to list of response DTOs.
     */
    List<CareerDocumentResponse> toDtoList(List<CareerDocument> documents);
}
