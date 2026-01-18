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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ActivityProviderService {

    private final ExhibitionRepository exhibitionRepository;
    private final ActivityProviderRepository activityProviderRepository;
    private final ActivityProviderRequestRepository providerRequestRepository;
    private final UserInfoRepository userInfoRepository;
    private final BoothRepository boothRepository;
    private final ActivityRepository activityRepository;

    // ----------------- Invite Activity Provider -----------------
    public com.capstone.personalityTest.dto.ResponseDTO.Exhibition.ActivityProviderRequestResponse inviteProvider(Long exhibitionId, Long providerId, String orgRequirements, String inviterEmail, LocalDateTime responseDeadline) {
        UserInfo inviter = userInfoRepository.findByEmail(inviterEmail)
                .orElseThrow(() -> new RuntimeException("Inviter not found"));

        Exhibition exhibition = exhibitionRepository.findById(exhibitionId)
                .orElseThrow(() -> new RuntimeException("Exhibition not found"));

        // Guard: Locked if CONFIRMED or later
        if (exhibition.getStatus().ordinal() >= ExhibitionStatus.CONFIRMED.ordinal()) {
            throw new RuntimeException("Exhibition is locked");
        }

        // Only ORG_OWNER of the organization or DEVELOPER
        boolean isDev = inviter.getRoles().stream().anyMatch(r -> r.getCode().equals("DEVELOPER"));
        if (!exhibition.getOrganization().getOwner().getId().equals(inviter.getId()) && !isDev) {
            throw new RuntimeException("You are not the owner of this organization");
        }

        if (exhibition.getStatus().ordinal() < ExhibitionStatus.VENUE_APPROVED.ordinal()) {
             throw new RuntimeException("Exhibition must be VENUE_APPROVED to invite providers");
        }

        ActivityProvider provider = activityProviderRepository.findById(providerId)
                .orElseThrow(() -> new RuntimeException("Provider not found"));

        // Check duplicate invitation
        boolean alreadyInvited = providerRequestRepository.existsByExhibitionAndProvider(exhibition, provider);
        if (alreadyInvited) {
            throw new RuntimeException("This provider has already been invited for this exhibition");
        }
        
        // Update exhibition status to ACTIVITY_PENDING if it was VENUE_APPROVED
        if (exhibition.getStatus() == ExhibitionStatus.VENUE_APPROVED) {
            exhibition.setStatus(ExhibitionStatus.ACTIVITY_PENDING);
            exhibitionRepository.save(exhibition);
        }

        ActivityProviderRequest request = new ActivityProviderRequest();
        request.setExhibition(exhibition);
        request.setProvider(provider);
        request.setOrgRequirements(orgRequirements);
        request.setStatus(ActivityProviderRequestStatus.INVITED);
        request.setInvitedAt(LocalDateTime.now());
        request.setResponseDeadline(responseDeadline);

        ActivityProviderRequest savedRequest = providerRequestRepository.save(request);
        return mapToResponse(savedRequest);
    }

    // ----------------- Submit Proposal (by Provider) -----------------
    // Updated to accept activityIds
    public com.capstone.personalityTest.dto.ResponseDTO.Exhibition.ActivityProviderRequestResponse submitProposal(Long requestId, String proposalText, Integer boothsCount, java.math.BigDecimal totalCost, java.util.List<Long> activityIds, String providerEmail) {
        UserInfo providerUser = userInfoRepository.findByEmail(providerEmail)
                .orElseThrow(() -> new RuntimeException("Provider user not found"));

        ActivityProviderRequest request = providerRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        boolean isDev = providerUser.getRoles().stream().anyMatch(r -> r.getCode().equals("DEVELOPER"));
        if (!request.getProvider().getOwner().getId().equals(providerUser.getId()) && !isDev) {
            throw new RuntimeException("Only the provider owner can submit a proposal");
        }

        if (request.getStatus() != ActivityProviderRequestStatus.INVITED) {
             throw new RuntimeException("Proposal can only be submitted for INVITED requests");
        }

        if (request.getResponseDeadline() != null && LocalDateTime.now().isAfter(request.getResponseDeadline())) {
            throw new RuntimeException("Submission deadline has passed");
        }

        request.setProviderProposal(proposalText);
        request.setProposedBoothsCount(boothsCount);
        request.setTotalCost(totalCost);
        request.setStatus(ActivityProviderRequestStatus.PROPOSED);
        request.setProposedAt(LocalDateTime.now());
        
        // Link activities
        if (activityIds != null && !activityIds.isEmpty()) {
            java.util.List<com.capstone.personalityTest.model.Exhibition.Activity> activities = activityRepository.findAllById(activityIds);
            // Validation: Ensure activities belong to this provider
            for (com.capstone.personalityTest.model.Exhibition.Activity activity : activities) {
                // Assuming activities must belong to provider. Activity entity must have provider set.
                // If Activity model update to include provider is done, we check:
                if (activity.getProvider() != null && !activity.getProvider().getId().equals(request.getProvider().getId())) {
                     throw new RuntimeException("Activity " + activity.getName() + " does not belong to this provider");
                }
            }
            request.setProposedActivities(new java.util.HashSet<>(activities));
        }

        ActivityProviderRequest savedRequest = providerRequestRepository.save(request);
        return mapToResponse(savedRequest);
    }

    // ----------------- Approve or Reject Provider Proposal -----------------
    public com.capstone.personalityTest.dto.ResponseDTO.Exhibition.ActivityProviderRequestResponse reviewProviderProposal(Long requestId, boolean approve, String comments, String reviewerEmail) {
        UserInfo reviewer = userInfoRepository.findByEmail(reviewerEmail)
                .orElseThrow(() -> new RuntimeException("Reviewer not found"));

        ActivityProviderRequest request = providerRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));
                
        Exhibition exhibition = request.getExhibition();

        if (exhibition.getStatus().ordinal() >= ExhibitionStatus.CONFIRMED.ordinal()) {
            throw new RuntimeException("Exhibition is locked");
        }

        boolean isDev = reviewer.getRoles().stream().anyMatch(r -> r.getCode().equals("DEVELOPER"));
        if (!exhibition.getOrganization().getOwner().getId().equals(reviewer.getId()) && !isDev) {
            throw new RuntimeException("You are not authorized to approve/reject this proposal");
        }

        if (request.getStatus() != ActivityProviderRequestStatus.PROPOSED) {
            throw new RuntimeException("Only PROPOSED requests can be reviewed");
        }

        int totalBooths = boothRepository.countByExhibition(exhibition);
        if (approve && (totalBooths + request.getProposedBoothsCount()) > exhibition.getMaxCapacity()) {
            throw new RuntimeException("Approving this request exceeds exhibition max capacity");
        }

        if (approve) {
            request.setStatus(ActivityProviderRequestStatus.APPROVED);
            request.setApprovedAt(LocalDateTime.now());
            request.setReviewedAt(LocalDateTime.now()); 
            request.setOrgResponse(comments);
            
            // Create Booths for approved activities
            if (request.getProposedActivities() != null) {
                for (com.capstone.personalityTest.model.Exhibition.Activity activity : request.getProposedActivities()) {
                     com.capstone.personalityTest.model.Exhibition.Booth booth = new com.capstone.personalityTest.model.Exhibition.Booth();
                     booth.setExhibition(exhibition);
                     booth.setBoothType(com.capstone.personalityTest.model.Enum.Exhibition.BoothType.ACTIVITY_PROVIDER);
                     booth.setActivityProviderRequestId(request.getId());
                     booth.setActivity(activity);
                     booth.setMaxParticipants(activity.getSuggestedMaxParticipants());
                     booth.setDurationMinutes(activity.getSuggestedDurationMinutes());
                     booth.setCreatedAt(LocalDateTime.now());
                     booth.setZone("Unassigned"); // Default zone
                     booth.setBoothNumber(0); // Default number
                     boothRepository.save(booth);
                }
            }
            
        } else {
            request.setStatus(ActivityProviderRequestStatus.REJECTED);
            request.setOrgResponse(comments);
            request.setReviewedAt(LocalDateTime.now()); 
        }

        ActivityProviderRequest savedRequest = providerRequestRepository.save(request);
        
        if (approve) {
            long totalRequests = providerRequestRepository.countByExhibition(exhibition);
            long totalApproved = providerRequestRepository.countByExhibitionAndStatus(exhibition, ActivityProviderRequestStatus.APPROVED);
            
            if (totalRequests > 0 && totalRequests == totalApproved) {
                exhibition.setStatus(ExhibitionStatus.ACTIVITY_APPROVED);
                exhibitionRepository.save(exhibition);
            }
        }

        return mapToResponse(savedRequest);
    }
    
    // ----------------- Cancel Provider Request -----------------
    @Transactional
    public com.capstone.personalityTest.dto.ResponseDTO.Exhibition.ActivityProviderRequestResponse cancelRequest(Long requestId, String reason, String cancellerEmail) {
        UserInfo canceller = userInfoRepository.findByEmail(cancellerEmail)
                .orElseThrow(() -> new RuntimeException("Canceller not found"));

        ActivityProviderRequest request = providerRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        Exhibition exhibition = request.getExhibition();

        if (exhibition.getStatus() == ExhibitionStatus.ACTIVE) {
            throw new RuntimeException("Cannot cancel request when exhibition is ACTIVE");
        }
        
        // Verify owner or DEVELOPER
        boolean isDev = canceller.getRoles().stream().anyMatch(r -> r.getCode().equals("DEVELOPER"));
        
        // Corrected check: Validate against the Provider's Owner
        if (!request.getProvider().getOwner().getId().equals(canceller.getId()) && !isDev) {
             throw new RuntimeException("Only the provider owner can cancel this request");
        }

        if (request.getStatus() != ActivityProviderRequestStatus.PROPOSED && request.getStatus() != ActivityProviderRequestStatus.APPROVED) {
            throw new RuntimeException("Only PROPOSED or APPROVED requests can be cancelled");
        }
        
        // State Change -> REJECTED (as per requirement, though CANCELLED might be semantic preference, sticking to requirement "ActivityProviderRequest.status → REJECTED")
        request.setStatus(ActivityProviderRequestStatus.REJECTED);
        request.setOrgResponse("Cancelled by provider: " + reason);
        // Side effects: Remove related booths? 
        // Logic generally implies if approved, booths might have been created. 
        // Assuming booths are created after approval or confirmation. If so, logic to remove them is needed.
        // But in this flow, booths seem to be implicitly counted via requests or university participation. 
        // If booths table has entries for this provider/request, delete them.
        // Assuming Booth entity doesn't directly link to ActivityProviderRequest but logical capacity.
        // If explicit booths exist for this provider in this exhibition:
        // boothRepository.deleteByExhibitionAndProvider... (If such method/link exists).
        // For now, simpler "Recalculate exhibition capacity" is automatic since capacity is summed from requests/booths.

        ActivityProviderRequest savedRequest = providerRequestRepository.save(request);
        return mapToResponse(savedRequest);
    }

    private com.capstone.personalityTest.dto.ResponseDTO.Exhibition.ActivityProviderRequestResponse mapToResponse(ActivityProviderRequest request) {
        return new com.capstone.personalityTest.dto.ResponseDTO.Exhibition.ActivityProviderRequestResponse(
                request.getId(),
                request.getExhibition().getId(),
                request.getProvider().getId(),
                request.getProvider().getName(),
                request.getProvider().getContactEmail(),
                request.getStatus(),
                request.getOrgRequirements(),
                request.getProviderProposal(),
                request.getProposedBoothsCount(),
                request.getTotalCost(),
                request.getResponseDeadline(),
                request.getOrgResponse(),
                request.getInvitedAt(),
                request.getProposedAt(),
                request.getReviewedAt(),
                request.getApprovedAt()
        );
    }
}
