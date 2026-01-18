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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MunicipalityService {

    private final VenueRequestRepository venueRequestRepository;
    private final ExhibitionRepository exhibitionRepository;
    private final UserInfoRepository userInfoRepository;
    private final VenueRepository venueRepository;

    // ----------------- Approve or Reject Venue Request -----------------
    public VenueRequest reviewVenueRequest(Long venueRequestId, boolean approve, String responseText, String reviewerEmail) {
        UserInfo reviewer = userInfoRepository.findByEmail(reviewerEmail)
                .orElseThrow(() -> new RuntimeException("Reviewer not found"));

        // Only municipality admin can review
        if (reviewer.getRoles().stream().noneMatch(r -> r.getCode().equals("MUNICIPALITY_ADMIN"))) {
            throw new RuntimeException("Only municipality admins can approve/reject venue requests");
        }

        VenueRequest request = venueRequestRepository.findById(venueRequestId)
                .orElseThrow(() -> new RuntimeException("Venue request not found"));

        Exhibition exhibition = request.getExhibition();
        Venue venue = request.getVenue();

        // Validate request is pending
        if (request.getStatus() != VenueRequestStatus.PENDING) {
            throw new RuntimeException("Venue request has already been reviewed");
        }

        // Validate response deadline
        if (request.getResponseDeadline().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Response deadline has passed for this request");
        }

        // Validate venue availability: no overlapping exhibitions
        List<VenueRequest> approvedRequests = venueRequestRepository
                .findByVenueIdAndStatus(venue.getId(), VenueRequestStatus.APPROVED);

        for (VenueRequest approved : approvedRequests) {
            LocalDate approvedStart = approved.getExhibition().getStartDate();
            LocalDate approvedEnd = approved.getExhibition().getEndDate();
            LocalDate currentStart = exhibition.getStartDate();
            LocalDate currentEnd = exhibition.getEndDate();

            if (currentStart.isBefore(approvedEnd.plusDays(1)) && currentEnd.isAfter(approvedStart.minusDays(1))) {
                throw new RuntimeException("Venue is already booked for overlapping dates");
            }
        }

        // Update request
        if (approve) {
            request.setStatus(VenueRequestStatus.APPROVED);
            request.setMunicipalityResponse(responseText);
            request.setReviewedAt(LocalDateTime.now());
            request.setReviewedByUser(reviewer);

            // Update exhibition status
            exhibition.setStatus(ExhibitionStatus.VENUE_APPROVED);
            exhibition.setUpdatedAt(LocalDateTime.now());
            exhibitionRepository.save(exhibition);

        } else {
            request.setStatus(VenueRequestStatus.REJECTED);
            request.setMunicipalityResponse(responseText);
            request.setReviewedAt(LocalDateTime.now());
            request.setReviewedByUser(reviewer);

            // Optionally: reset exhibition to DRAFT so org can submit another venue request
            exhibition.setStatus(ExhibitionStatus.DRAFT);
            exhibition.setUpdatedAt(LocalDateTime.now());
            exhibitionRepository.save(exhibition);
        }

        return venueRequestRepository.save(request);
    }
}
