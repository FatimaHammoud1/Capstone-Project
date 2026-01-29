package com.capstone.personalityTest.mapper;

import com.capstone.personalityTest.dto.RequestDTO.test.MetricRequest;
import com.capstone.personalityTest.dto.ResponseDTO.test.MetricResponse;
import com.capstone.personalityTest.model.testm.Test.Metric;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface MetricMapper {

    @Mapping(target = "id", ignore = true)
    Metric toEntity(MetricRequest metricRequest);

    @Mapping(source = "baseTest.id", target = "baseTestId")
    @Mapping(source = "baseTest.code", target = "baseTestCode")
    MetricResponse toDto(Metric metric);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "baseTest", ignore = true)
    void updateMetricFromDto(MetricRequest request, @MappingTarget Metric metric);
}
