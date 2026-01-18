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

@Service
@RequiredArgsConstructor
public class VenueRequestService {

    private final ExhibitionRepository exhibitionRepository;
    private final VenueRepository venueRepository;
    private final VenueRequestRepository venueRequestRepository;
    private final UserInfoRepository userInfoRepository;

    // ----------------- Create Venue Request -----------------
    public VenueRequest createVenueRequest(Long exhibitionId, Long venueId, String orgNotes, LocalDateTime responseDeadline, String creatorEmail) {
        UserInfo creator = userInfoRepository.findByEmail(creatorEmail)
                .orElseThrow(() -> new RuntimeException("Creator not found"));

        Exhibition exhibition = exhibitionRepository.findById(exhibitionId)
                .orElseThrow(() -> new RuntimeException("Exhibition not found"));

        // Check org owner
        if (!exhibition.getOrganization().getOwner().getId().equals(creator.getId())) {
            throw new RuntimeException("Only organization owner can request a venue");
        }

        // Status must be DRAFT
        if (exhibition.getStatus() != ExhibitionStatus.DRAFT) {
            throw new RuntimeException("Venue request can only be created for DRAFT exhibitions");
        }

        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new RuntimeException("Venue not found"));

        // Create VenueRequest
        VenueRequest request = new VenueRequest();
        request.setExhibition(exhibition);
        request.setVenue(venue);
        request.setOrgNotes(orgNotes);
        request.setStatus(VenueRequestStatus.PENDING);
        request.setRequestedAt(LocalDateTime.now());
        request.setResponseDeadline(responseDeadline); // enforce deadline
        venueRequestRepository.save(request);

        // Update Exhibition status
        exhibition.setStatus(ExhibitionStatus.VENUE_PENDING);
        exhibition.setUpdatedAt(LocalDateTime.now());
        exhibitionRepository.save(exhibition);

        return request;
    }

    // Optional: Get all requests for this exhibition
    public List<VenueRequest> getRequestsForExhibition(Long exhibitionId) {
        return venueRequestRepository.findByExhibitionId(exhibitionId);
    }
}
