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

import com.capstone.personalityTest.dto.ResponseDTO.Exhibition.ActivityProviderResponse;
import java.util.List;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import com.capstone.personalityTest.dto.ResponseDTO.Exhibition.ActivityProviderRequestResponse;
import com.capstone.personalityTest.model.Enum.Exhibition.BoothType;
import com.capstone.personalityTest.model.Exhibition.Activity;
import com.capstone.personalityTest.model.Exhibition.Booth;

@Service
@RequiredArgsConstructor
public class ActivityProviderService {

    private final ExhibitionRepository exhibitionRepository;
    private final ActivityProviderRepository activityProviderRepository;
    private final ActivityProviderRequestRepository providerRequestRepository;
    private final UserInfoRepository userInfoRepository;
    private final BoothRepository boothRepository;
    private final ActivityRepository activityRepository;
    private final ExhibitionService exhibitionService;


    // ----------------- Invite Activity Provider -----------------
    public ActivityProviderRequestResponse inviteProvider(Long exhibitionId, Long providerId, String orgRequirements, String inviterEmail, LocalDateTime responseDeadline) {
        UserInfo inviter = userInfoRepository.findByEmail(inviterEmail)
                .orElseThrow(() -> new RuntimeException("Inviter not found"));

        Exhibition exhibition = exhibitionRepository.findById(exhibitionId)
                .orElseThrow(() -> new RuntimeException("Exhibition not found"));

        // Only ORG_OWNER of the organization or DEVELOPER
        boolean isDev = inviter.getRoles().stream().anyMatch(r -> r.getCode().equals("DEVELOPER"));
        if (!exhibition.getOrganization().getOwner().getId().equals(inviter.getId()) && !isDev) {
            throw new RuntimeException("You are not the owner of this organization");
        }

        // Validate exhibition status: must be VENUE_APPROVED or PLANNING
        if (exhibition.getStatus() != ExhibitionStatus.VENUE_APPROVED && exhibition.getStatus() != ExhibitionStatus.PLANNING) {
            throw new RuntimeException("Can only invite providers after venue is approved");
        }
        
        // Transition to PLANNING if first invitation
        if (exhibition.getStatus() == ExhibitionStatus.VENUE_APPROVED) {
            exhibition.setStatus(ExhibitionStatus.PLANNING);
            exhibitionRepository.save(exhibition);
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

        ActivityProviderRequest savedRequest = providerRequestRepository.save(request);
        return mapToResponse(savedRequest);
    }

    // ----------------- Submit Proposal (by Provider) -----------------
    // Updated to accept activityIds
    public ActivityProviderRequestResponse submitProposal(Long requestId, String proposalText, Integer boothsCount, BigDecimal totalCost, List<Long> activityIds, String providerEmail) {
        UserInfo providerUser = userInfoRepository.findByEmail(providerEmail)
                .orElseThrow(() -> new RuntimeException("Provider user not found"));

        ActivityProviderRequest request = providerRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        boolean isDev = providerUser.getRoles().stream().anyMatch(r -> r.getCode().equals("DEVELOPER"));
        if (!request.getProvider().getOwner().getId().equals(providerUser.getId()) && !isDev) {
            throw new RuntimeException("Only the provider owner can submit a proposal");
        }

        // Allow proposal submission if INVITED or REJECTED (can re-submit after rejection)
        if (request.getStatus() != ActivityProviderRequestStatus.INVITED && request.getStatus() != ActivityProviderRequestStatus.REJECTED) {
             throw new RuntimeException("Proposal can only be submitted for INVITED or REJECTED requests");
        }

        // Validate response deadline - auto-cancel if passed
        if (request.getResponseDeadline() != null && LocalDateTime.now().isAfter(request.getResponseDeadline())) {
            request.setStatus(ActivityProviderRequestStatus.CANCELLED);
            providerRequestRepository.save(request);
            throw new RuntimeException("Submission deadline has passed. Your invitation has been automatically cancelled.");
        }

        request.setProviderProposal(proposalText);
        request.setProposedBoothsCount(boothsCount);
        request.setTotalCost(totalCost);
        request.setStatus(ActivityProviderRequestStatus.PROPOSED);
        request.setProposedAt(LocalDateTime.now());
        
        // Link activities
        if (activityIds != null && !activityIds.isEmpty()) {
            List<Activity> activities = activityRepository.findAllById(activityIds);
            // Validation: Ensure activities belong to this provider
            for (Activity activity : activities) {
                // Assuming activities must belong to provider. Activity entity must have provider set.
                // If Activity model update to include provider is done, we check:
                if (activity.getProvider() != null && !activity.getProvider().getId().equals(request.getProvider().getId())) {
                     throw new RuntimeException("Activity " + activity.getName() + " does not belong to this provider");
                }
            }
            request.setProposedActivities(new HashSet<>(activities));
        }

        ActivityProviderRequest savedRequest = providerRequestRepository.save(request);
        return mapToResponse(savedRequest);
    }

    // ----------------- Approve or Reject Provider Proposal -----------------
    public ActivityProviderRequestResponse reviewProviderProposal(Long requestId, boolean approve, LocalDateTime confirmationDeadline, String comments, String reviewerEmail) {
        UserInfo reviewer = userInfoRepository.findByEmail(reviewerEmail)
                .orElseThrow(() -> new RuntimeException("Reviewer not found"));

        ActivityProviderRequest request = providerRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));
                
        Exhibition exhibition = request.getExhibition();

        // Validate exhibition status
        if (exhibition.getStatus() != ExhibitionStatus.PLANNING) {
            throw new RuntimeException("Can only review proposals during PLANNING phase");
        }

        boolean isDev = reviewer.getRoles().stream().anyMatch(r -> r.getCode().equals("DEVELOPER"));
        if (!exhibition.getOrganization().getOwner().getId().equals(reviewer.getId()) && !isDev) {
            throw new RuntimeException("You are not authorized to approve/reject this proposal");
        }

        if (request.getStatus() != ActivityProviderRequestStatus.PROPOSED) {
            throw new RuntimeException("Only PROPOSED requests can be reviewed");
        }

        if (approve) {
            Integer proposedBooths = request.getProposedBoothsCount();
            
            // Validate proposed booths is positive
            if (proposedBooths == null || proposedBooths <= 0) {
                throw new RuntimeException("Proposed booths count is invalid");
            }
            
            // Validate per-provider booth limit (if set)
            if (exhibition.getMaxBoothsPerProvider() != null && proposedBooths > exhibition.getMaxBoothsPerProvider()) {
                throw new RuntimeException("Proposed booths (" + proposedBooths + ") exceeds max per provider (" + exhibition.getMaxBoothsPerProvider() + ")");
            }
            
            // Validate total available booths
            int totalBooths = boothRepository.countByExhibition(exhibition);
            int remainingBooths = exhibition.getTotalAvailableBooths() - totalBooths;
            
            if (proposedBooths > remainingBooths) {
                throw new RuntimeException("Proposed booths (" + proposedBooths + ") exceeds remaining capacity (" + remainingBooths + ")");
            }
            
            request.setStatus(ActivityProviderRequestStatus.APPROVED);
            request.setConfirmationDeadline(confirmationDeadline);
            request.setApprovedAt(LocalDateTime.now());
            request.setReviewedAt(LocalDateTime.now()); 
            request.setOrgResponse(comments);
            
            // Create Booths for approved activities
            if (request.getProposedActivities() != null) {
                for (Activity activity : request.getProposedActivities()) {
                     Booth booth = new Booth();
                     booth.setExhibition(exhibition);
                     booth.setBoothType(BoothType.ACTIVITY_PROVIDER);
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

        return mapToResponse(savedRequest);
    }
    
    // ----------------- Confirm Provider Commitment -----------------
    public ActivityProviderRequestResponse confirmProvider(Long requestId, String providerEmail) {
        UserInfo providerUser = userInfoRepository.findByEmail(providerEmail)
                .orElseThrow(() -> new RuntimeException("Provider user not found"));

        ActivityProviderRequest request = providerRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));
        
        Exhibition exhibition = request.getExhibition();

        boolean isDev = providerUser.getRoles().stream().anyMatch(r -> r.getCode().equals("DEVELOPER"));
        if (!request.getProvider().getOwner().getId().equals(providerUser.getId()) && !isDev) {
            throw new RuntimeException("Only the provider owner can confirm commitment");
        }

        // Validate exhibition status
        if (exhibition.getStatus() != ExhibitionStatus.PLANNING) {
            throw new RuntimeException("Can only confirm during PLANNING phase");
        }

        if (request.getStatus() != ActivityProviderRequestStatus.APPROVED) {
            throw new RuntimeException("Only APPROVED requests can be confirmed");
        }

        // Validate confirmation deadline - auto-cancel if passed
        if (request.getConfirmationDeadline() != null && LocalDateTime.now().isAfter(request.getConfirmationDeadline())) {
            request.setStatus(ActivityProviderRequestStatus.CANCELLED);
            providerRequestRepository.save(request);
            throw new RuntimeException("Confirmation deadline has passed. Your request has been automatically cancelled.");
        }

        request.setStatus(ActivityProviderRequestStatus.CONFIRMED);
        // Can add confirmedAt timestamp if needed
        
        ActivityProviderRequest savedRequest = providerRequestRepository.save(request);
        return mapToResponse(savedRequest);
    }
    
    // ----------------- Finalize Participation (After Schedule) -----------------
    public ActivityProviderRequestResponse finalizeParticipation(Long requestId, String providerEmail) {
        UserInfo providerUser = userInfoRepository.findByEmail(providerEmail)
                .orElseThrow(() -> new RuntimeException("Provider user not found"));

        ActivityProviderRequest request = providerRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));
        
        Exhibition exhibition = request.getExhibition();

        boolean isDev = providerUser.getRoles().stream().anyMatch(r -> r.getCode().equals("DEVELOPER"));
        if (!request.getProvider().getOwner().getId().equals(providerUser.getId()) && !isDev) {
            throw new RuntimeException("Only the provider owner can finalize participation");
        }

        if (exhibition.getStatus() != ExhibitionStatus.CONFIRMED) {
            throw new RuntimeException("Cannot finalize participation before exhibition is CONFIRMED (schedule ready)");
        }

        if (request.getStatus() != ActivityProviderRequestStatus.CONFIRMED) {
            throw new RuntimeException("Only CONFIRMED requests can be finalized");
        }
        
        // Validate finalization deadline - auto-cancel if passed
        if (exhibition.getFinalizationDeadline() != null && LocalDateTime.now().isAfter(exhibition.getFinalizationDeadline())) {
            request.setStatus(ActivityProviderRequestStatus.CANCELLED);
            providerRequestRepository.save(request);
            throw new RuntimeException("Finalization deadline has passed. Your request has been automatically cancelled.");
        }

        request.setStatus(ActivityProviderRequestStatus.FINALIZED);
        request.setFinalizedAt(LocalDateTime.now());
        
        ActivityProviderRequest savedRequest = providerRequestRepository.save(request);
        return mapToResponse(savedRequest);
    }
    
    // ----------------- Cancel Provider Request -----------------
    @Transactional
    public ActivityProviderRequestResponse cancelRequest(Long requestId, String reason, String cancellerEmail) {
        UserInfo canceller = userInfoRepository.findByEmail(cancellerEmail)
                .orElseThrow(() -> new RuntimeException("Canceller not found"));

        ActivityProviderRequest request = providerRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        Exhibition exhibition = request.getExhibition();

        if (exhibition.getStatus() == ExhibitionStatus.ACTIVE) {
            throw new RuntimeException("Cannot cancel request when exhibition is ACTIVE");
        }
        
        // Check if canceller is the Provider Owner or Org Owner
        boolean isProviderOwner = request.getProvider().getOwner().getId().equals(canceller.getId());
        boolean isOrgOwner = exhibition.getOrganization().getOwner().getId().equals(canceller.getId());
        boolean isDev = canceller.getRoles().stream().anyMatch(r -> r.getCode().equals("DEVELOPER"));
        
        if (!isProviderOwner && !isOrgOwner && !isDev) {
             throw new RuntimeException("Not authorized to cancel this request");
        }
        
        request.setStatus(ActivityProviderRequestStatus.CANCELLED);
        request.setOrgResponse("Cancelled: " + reason);
        
        // Side effects: Remove related booths
        List<Booth> booths = boothRepository.findByActivityProviderRequestId(request.getId());
        boothRepository.deleteAll(booths);

        ActivityProviderRequest savedRequest = providerRequestRepository.save(request);
        return mapToResponse(savedRequest);
    }

    // ----------------- Get Requests -----------------
    public ActivityProviderRequestResponse getRequestById(Long requestId) {
        ActivityProviderRequest request = providerRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));
        return mapToResponse(request);
    }

    public List<ActivityProviderRequestResponse> getRequestsByExhibition(Long exhibitionId) {
        Exhibition exhibition = exhibitionRepository.findById(exhibitionId)
                .orElseThrow(() -> new RuntimeException("Exhibition not found"));

        return providerRequestRepository.findAll().stream()
                .filter(r -> r.getExhibition().getId().equals(exhibitionId))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<ActivityProviderRequestResponse> getRequestsByProvider(Long providerId) {
        // Ensure provider exists
        activityProviderRepository.findById(providerId)
            .orElseThrow(() -> new RuntimeException("Provider not found"));

        return providerRequestRepository.findByProviderId(providerId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private ActivityProviderRequestResponse mapToResponse(ActivityProviderRequest request) {
        return new ActivityProviderRequestResponse(
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
    
    // ----------------- Get All Active Providers -----------------
    public List<ActivityProviderResponse> getAllActiveProviders() {
        return activityProviderRepository.findAll().stream()
                .filter(provider -> Boolean.TRUE.equals(provider.getActive()))
                .map(this::mapToProviderResponse)
                .collect(Collectors.toList());
    }

    // ----------------- Get Provider By ID -----------------
    public ActivityProviderResponse getProviderById(Long providerId) {
        ActivityProvider provider = activityProviderRepository.findById(providerId)
                .orElseThrow(() -> new RuntimeException("Provider not found"));
        return mapToProviderResponse(provider);
    }

    // ----------------- Get Providers By Owner ID -----------------
    public List<ActivityProviderResponse> getActivityProvidersByOwnerId(Long ownerId) {
        return activityProviderRepository.findAllByOwnerId(ownerId).stream()
                .map(this::mapToProviderResponse)
                .collect(Collectors.toList());
    }

    private ActivityProviderResponse mapToProviderResponse(ActivityProvider provider) {
        return new ActivityProviderResponse(
                provider.getId(),
                provider.getName(),
                provider.getContactEmail(),
                provider.getContactPhone(),
                provider.getActive(),
                provider.getOwner() != null ? provider.getOwner().getId() : null
        );
    }
}
