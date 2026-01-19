package com.capstone.personalityTest.service.Exhibition;

import com.capstone.personalityTest.dto.RequestDTO.Exhibition.ScheduleUpdateRequest;
import com.capstone.personalityTest.dto.ResponseDTO.Exhibition.BoothResponse;
import com.capstone.personalityTest.model.Enum.Exhibition.BoothType;
import com.capstone.personalityTest.model.Exhibition.Booth;
import com.capstone.personalityTest.model.Exhibition.Exhibition;
import com.capstone.personalityTest.model.UserInfo;
import com.capstone.personalityTest.repository.Exhibition.BoothRepository;
import com.capstone.personalityTest.repository.Exhibition.ExhibitionRepository;
import com.capstone.personalityTest.repository.UserInfoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BoothService {

    private final BoothRepository boothRepository;
    private final ExhibitionRepository exhibitionRepository;
    private final UserInfoRepository userInfoRepository;

    // ----------------- Get Booths for Exhibition -----------------
    // ----------------- Get Booths for Exhibition -----------------
    public List<BoothResponse> getBoothsByExhibition(Long exhibitionId) {
        Exhibition exhibition = exhibitionRepository.findById(exhibitionId)
                .orElseThrow(() -> new RuntimeException("Exhibition not found"));

        // No security check needed for GET usually, or minimal (anyone can see booths?) 
        // Logic: Return all booths converted to DTOs
        // We lack a direct 'findByExhibition' in Repo interface in snippets, assuming standard JpaRepository derivation works or we need to add it.
        // Actually we have `countByExhibition` and `deleteByExhibition` in Repo snippets, let's assume `findByExhibition` needs adding or derivation.
        // Assuming we need to add `List<Booth> findByExhibition(Exhibition exhibition);` to repo first.
        
        // Wait, standard JPA method is findByExhibition
        return boothRepository.findAll().stream() // Ideally findByExhibition(exhibition)
                .filter(b -> b.getExhibition().getId().equals(exhibitionId)) // fallback filtering
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public BoothResponse getBoothById(Long boothId) {
        Booth booth = boothRepository.findById(boothId)
                .orElseThrow(() -> new RuntimeException("Booth not found"));
        return mapToResponse(booth);
    }

    // ----------------- Update Booth Assignments (Zone/Number) -----------------
    public void updateBoothAllocations(Long exhibitionId, ScheduleUpdateRequest request, String orgOwnerEmail) {
        UserInfo orgOwner = userInfoRepository.findByEmail(orgOwnerEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Exhibition exhibition = exhibitionRepository.findById(exhibitionId)
                .orElseThrow(() -> new RuntimeException("Exhibition not found"));

        boolean isDev = orgOwner.getRoles().stream().anyMatch(r -> r.getCode().equals("DEVELOPER"));
        if (!exhibition.getOrganization().getOwner().getId().equals(orgOwner.getId()) && !isDev) {
            throw new RuntimeException("Only ORG_OWNER can update booth allocations");
        }

        // Update schedule JSON if provided
        if (request.getScheduleJson() != null) {
            exhibition.setScheduleJson(request.getScheduleJson());
            exhibitionRepository.save(exhibition);
        }

        // Update individual booths
        if (request.getBoothAllocations() != null) {
            for (ScheduleUpdateRequest.BoothAllocation allocation : request.getBoothAllocations()) {
                Booth booth = boothRepository.findById(allocation.getBoothId())
                        .orElseThrow(() -> new RuntimeException("Booth with ID " + allocation.getBoothId() + " not found"));
                
                if (!booth.getExhibition().getId().equals(exhibitionId)) {
                    throw new RuntimeException("Booth " + allocation.getBoothId() + " does not belong to exhibition " + exhibitionId);
                }

                if (allocation.getZone() != null) booth.setZone(allocation.getZone());
                if (allocation.getBoothNumber() != null) booth.setBoothNumber(allocation.getBoothNumber());
                
                boothRepository.save(booth);
            }
        }
    }

    private BoothResponse mapToResponse(Booth booth) {
        return new BoothResponse(
            booth.getId(),
            booth.getExhibition().getId(),
            booth.getBoothType().name(),
            booth.getUniversityParticipationId(),
            booth.getActivityProviderRequestId(),
            booth.getActivity() != null ? booth.getActivity().getId() : null,
            booth.getZone(),
            booth.getBoothNumber(),
            booth.getDurationMinutes(),
            booth.getMaxParticipants(),
            booth.getCreatedAt()
        );
    }
}
