package com.capstone.personalityTest.service.Exhibition;

import com.capstone.personalityTest.model.Enum.Exhibition.ExhibitionStatus;
import com.capstone.personalityTest.model.Exhibition.Exhibition;
import com.capstone.personalityTest.model.Exhibition.Organization;
import com.capstone.personalityTest.model.UserInfo;
import com.capstone.personalityTest.repository.Exhibition.ExhibitionRepository;
import com.capstone.personalityTest.repository.Exhibition.OrganizationRepository;
import com.capstone.personalityTest.repository.UserInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExhibitionService {

    private final ExhibitionRepository exhibitionRepository;
    private final OrganizationRepository organizationRepository;
    private final UserInfoRepository userInfoRepository;

    // ----------------- Create Exhibition -----------------
    public Exhibition createExhibition(Long orgId, Exhibition exhibition, String creatorEmail) {
        UserInfo creator = userInfoRepository.findByEmail(creatorEmail)
                .orElseThrow(() -> new RuntimeException("Creator not found"));

        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));

        // Only owner of the organization can create
        if (!org.getOwner().getId().equals(creator.getId())) {
            throw new RuntimeException("You are not the owner of this organization");
        }

        // Set initial status
        exhibition.setOrganization(org);
        exhibition.setStatus(ExhibitionStatus.DRAFT);
        exhibition.setCreatedAt(LocalDateTime.now());
        exhibition.setUpdatedAt(LocalDateTime.now());

        return exhibitionRepository.save(exhibition);
    }

    // Optional: get all exhibitions of this org
    public List<Exhibition> getExhibitionsByOrg(Long orgId) {
        return exhibitionRepository.findByOrganizationId(orgId);
    }
}
