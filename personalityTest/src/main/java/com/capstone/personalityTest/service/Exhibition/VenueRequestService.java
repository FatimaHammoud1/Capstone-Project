package com.capstone.personalityTest.service.Exhibition;

import com.capstone.personalityTest.model.Enum.Exhibition.ExhibitionStatus;
import com.capstone.personalityTest.model.Enum.Exhibition.VenueRequestStatus;
import com.capstone.personalityTest.model.Exhibition.Exhibition;
import com.capstone.personalityTest.model.Exhibition.Venue;
import com.capstone.personalityTest.model.Exhibition.VenueRequest;
import com.capstone.personalityTest.model.UserInfo;
import com.capstone.personalityTest.repository.Exhibition.ExhibitionRepository;
import com.capstone.personalityTest.repository.Exhibition.VenueRepository;
import com.capstone.personalityTest.repository.Exhibition.VenueRequestRepository;
import com.capstone.personalityTest.repository.UserInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import com.capstone.personalityTest.dto.ResponseDTO.Exhibition.VenueRequestResponse;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VenueRequestService {

    private final ExhibitionRepository exhibitionRepository;
    private final VenueRepository venueRepository;
    private final VenueRequestRepository venueRequestRepository;
    private final UserInfoRepository userInfoRepository;

    // ----------------- Create Venue Request -----------------
    public VenueRequestResponse createVenueRequest(Long exhibitionId, Long venueId, String orgNotes, LocalDateTime responseDeadline, String creatorEmail) {
        UserInfo creator = userInfoRepository.findByEmail(creatorEmail)
                .orElseThrow(() -> new RuntimeException("Creator not found"));

        Exhibition exhibition = exhibitionRepository.findById(exhibitionId)
                .orElseThrow(() -> new RuntimeException("Exhibition not found"));

        // Check org owner OR developer
        boolean isDev = creator.getRoles().stream().anyMatch(r -> r.getCode().equals("DEVELOPER"));
        if (!exhibition.getOrganization().getOwner().getId().equals(creator.getId()) && !isDev) {
            throw new RuntimeException("Only organization owner can request a venue");
        }

        // Status must be DRAFT
        if (exhibition.getStatus() != ExhibitionStatus.DRAFT) {
            throw new RuntimeException("Venue request can only be created for DRAFT exhibitions");
        }

        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new RuntimeException("Venue not found"));

        // Check if venue is available (not reserved)
        if (venue.getAvailable() != null && !venue.getAvailable()) {
            throw new RuntimeException("Venue is not available - already reserved by another organization");
        }

        // Create VenueRequest
        VenueRequest request = new VenueRequest();
        request.setExhibition(exhibition);
        request.setVenue(venue);
        request.setOrgNotes(orgNotes);
        request.setStatus(VenueRequestStatus.PENDING);
        request.setRequestedAt(LocalDateTime.now());
        request.setResponseDeadline(responseDeadline); // enforce deadline
        VenueRequest savedRequest = venueRequestRepository.save(request);

        // Update Exhibition status
        exhibition.setStatus(ExhibitionStatus.VENUE_PENDING);
        exhibition.setUpdatedAt(LocalDateTime.now());
        exhibitionRepository.save(exhibition);

        return mapToResponse(savedRequest);
    }

    // Optional: Get all requests for this exhibition
    public List<VenueRequestResponse> getRequestsForExhibition(Long exhibitionId) {
        return venueRequestRepository.findByExhibitionId(exhibitionId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<VenueRequestResponse> getAllRequests() {
        return venueRequestRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private VenueRequestResponse mapToResponse(VenueRequest request) {
        return new VenueRequestResponse(
                request.getId(),
                request.getExhibition().getId(),
                request.getVenue().getId(),
                request.getVenue().getName(),
                request.getVenue().getAddress(),
                request.getStatus(),
                request.getOrgNotes(),
                request.getMunicipalityResponse(),
                request.getResponseDeadline(),
                request.getRequestedAt(),
                request.getReviewedAt()
        );
    }
}
