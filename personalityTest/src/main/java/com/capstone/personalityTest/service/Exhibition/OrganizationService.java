package com.capstone.personalityTest.service.Exhibition;

import com.capstone.personalityTest.dto.ResponseDTO.Exhibition.OrganizationResponse;
import com.capstone.personalityTest.model.Exhibition.Organization;
import com.capstone.personalityTest.repository.Exhibition.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationRepository organizationRepository;

    public List<OrganizationResponse> getAllOrganizations() {
        return organizationRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private OrganizationResponse mapToResponse(Organization org) {
        return new OrganizationResponse(
                org.getId(),
                org.getName(),
                org.getDescription(),
                org.getType(),
                org.getActive(),
                org.getOwner() != null ? org.getOwner().getId() : null
        );
    }
}
