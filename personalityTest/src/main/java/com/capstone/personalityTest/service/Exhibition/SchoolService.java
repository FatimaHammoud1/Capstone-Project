package com.capstone.personalityTest.service.Exhibition;

import com.capstone.personalityTest.dto.ResponseDTO.Exhibition.SchoolResponse;
import com.capstone.personalityTest.model.Exhibition.School;
import com.capstone.personalityTest.repository.Exhibition.SchoolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SchoolService {

    private final SchoolRepository schoolRepository;

    public List<SchoolResponse> getAllSchools() {
        return schoolRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private SchoolResponse mapToResponse(School school) {
        return new SchoolResponse(
                school.getId(),
                school.getName(),
                school.getContactEmail(),
                school.getContactPhone(),
                school.getActive(),
                school.getOwner() != null ? school.getOwner().getId() : null
        );
    }
}
