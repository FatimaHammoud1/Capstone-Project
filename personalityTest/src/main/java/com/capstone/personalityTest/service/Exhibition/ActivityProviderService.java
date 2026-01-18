package com.capstone.personalityTest.service.Exhibition;

import com.capstone.personalityTest.model.Enum.Exhibition.ActivityProviderRequestStatus;
import com.capstone.personalityTest.model.Enum.Exhibition.ExhibitionStatus;
import com.capstone.personalityTest.model.Exhibition.ActivityProvider;
import com.capstone.personalityTest.model.Exhibition.ActivityProviderRequest;
import com.capstone.personalityTest.model.Exhibition.Exhibition;
import com.capstone.personalityTest.model.UserInfo;
import com.capstone.personalityTest.repository.Exhibition.*;
import com.capstone.personalityTest.repository.UserInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ActivityProviderService {

    private final ExhibitionRepository exhibitionRepository;
    private final ActivityProviderRepository activityProviderRepository;
    private final ActivityProviderRequestRepository providerRequestRepository;
    private final UserInfoRepository userInfoRepository;
    private final BoothRepository boothRepository;

    // ----------------- Invite Activity Provider -----------------
    public ActivityProviderRequest inviteProvider(Long exhibitionId, Long providerId, String orgRequirements, String inviterEmail, LocalDateTime responseDeadline) {
        UserInfo inviter = userInfoRepository.findByEmail(inviterEmail)
                .orElseThrow(() -> new RuntimeException("Inviter not found"));

        Exhibition exhibition = exhibitionRepository.findById(exhibitionId)
                .orElseThrow(() -> new RuntimeException("Exhibition not found"));

        // Only ORG_OWNER of the organization
        if (!exhibition.getOrganization().getOwner().getId().equals(inviter.getId())) {
            throw new RuntimeException("You are not the owner of this organization");
        }

        if (exhibition.getStatus() != ExhibitionStatus.VENUE_APPROVED) {
            throw new RuntimeException("Exhibition must be VENUE_APPROVED to invite providers");
        }

        ActivityProvider provider = activityProviderRepository.findById(providerId)
                .orElseThrow(() -> new RuntimeException("Provider not found"));

        // Check duplicate invitation
        boolean alreadyInvited = providerRequestRepository.existsByExhibitionAndProvider(exhibition, provider);
        if (alreadyInvited) {
            throw new RuntimeException("This provider has already been invited for this exhibition");
        }

        ActivityProviderRequest request = new ActivityProviderRequest();
        request.setExhibition(exhibition);
        request.setProvider(provider);
        request.setOrgRequirements(orgRequirements);
        request.setStatus(ActivityProviderRequestStatus.INVITED);
        request.setInvitedAt(LocalDateTime.now());
        request.setResponseDeadline(responseDeadline);

        return providerRequestRepository.save(request);
    }

    // ----------------- Approve or Reject Provider Proposal -----------------
    public ActivityProviderRequest reviewProviderProposal(Long requestId, boolean approve, String comments, String reviewerEmail) {
        UserInfo reviewer = userInfoRepository.findByEmail(reviewerEmail)
                .orElseThrow(() -> new RuntimeException("Reviewer not found"));

        Exhibition exhibition = providerRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"))
                .getExhibition();

        // Only ORG_OWNER can approve/reject proposals
        if (!exhibition.getOrganization().getOwner().getId().equals(reviewer.getId())) {
            throw new RuntimeException("You are not authorized to approve/reject this proposal");
        }

        ActivityProviderRequest request = providerRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if (request.getStatus() != ActivityProviderRequestStatus.PROPOSED) {
            throw new RuntimeException("Only PROPOSED requests can be reviewed");
        }

        // Optional: validate total booths do not exceed exhibition max capacity
        int totalBooths = boothRepository.countByExhibition(exhibition);
        if (approve && (totalBooths + request.getProposedBoothsCount()) > exhibition.getMaxCapacity()) {
            throw new RuntimeException("Approving this request exceeds exhibition max capacity");
        }

        if (approve) {
            request.setStatus(ActivityProviderRequestStatus.APPROVED);
            request.setApprovedAt(LocalDateTime.now());
        } else {
            request.setStatus(ActivityProviderRequestStatus.REJECTED);
            request.setRejectionReason(comments);
        }

        return providerRequestRepository.save(request);
    }
}
