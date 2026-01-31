package com.capstone.personalityTest.service.Exhibition;

import com.capstone.personalityTest.model.Enum.Exhibition.ExhibitionStatus;
import com.capstone.personalityTest.model.Enum.Exhibition.VenueRequestStatus;
import com.capstone.personalityTest.model.Exhibition.Exhibition;
import com.capstone.personalityTest.model.Exhibition.Venue;
import com.capstone.personalityTest.model.Exhibition.VenueRequest;
import com.capstone.personalityTest.model.UserInfo;
import com.capstone.personalityTest.repository.Exhibition.ExhibitionRepository;
import com.capstone.personalityTest.repository.Exhibition.MunicipalityRepository;
import com.capstone.personalityTest.repository.Exhibition.VenueRepository;
import com.capstone.personalityTest.repository.Exhibition.VenueRequestRepository;
import com.capstone.personalityTest.repository.UserInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import com.capstone.personalityTest.dto.ResponseDTO.Exhibition.VenueRequestResponse;
import com.capstone.personalityTest.dto.ResponseDTO.Exhibition.MunicipalityResponse;
import com.capstone.personalityTest.model.Exhibition.Municipality;
import java.util.stream.Collectors;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MunicipalityService {

    private final VenueRequestRepository venueRequestRepository;
    private final ExhibitionRepository exhibitionRepository;
    private final UserInfoRepository userInfoRepository;
    private final VenueRepository venueRepository;
    private final MunicipalityRepository municipalityRepository;

    // ----------------- Approve or Reject Venue Request -----------------
    public VenueRequestResponse reviewVenueRequest(Long venueRequestId, boolean approve, String responseText, String reviewerEmail) {
        UserInfo reviewer = userInfoRepository.findByEmail(reviewerEmail)
                .orElseThrow(() -> new RuntimeException("Reviewer not found"));

        // Only municipality admin can review OR developer
        boolean isDev = reviewer.getRoles().stream().anyMatch(r -> r.getCode().equals("DEVELOPER"));
        if (reviewer.getRoles().stream().noneMatch(r -> r.getCode().equals("MUNICIPALITY_ADMIN")) && !isDev) {
            throw new RuntimeException("Only municipality admins can approve/reject venue requests");
        }

        VenueRequest request = venueRequestRepository.findById(venueRequestId)
                .orElseThrow(() -> new RuntimeException("Venue request not found"));

        Exhibition exhibition = request.getExhibition();
        Venue venue = request.getVenue();

        // Validate venue is not manually closed/unavailable
        if (Boolean.FALSE.equals(venue.getAvailable())) {
             throw new RuntimeException("Venue is currently marked as unavailable/closed");
        }

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

            // Calculate available booths from venue space
            Double venueSqm = venue.getSpaceSqm();
            Double boothSqm = exhibition.getEffectiveBoothSqm(); // uses default if not set
            
            if (venueSqm != null && boothSqm != null && boothSqm > 0) {
                int availableBooths = (int) Math.floor(venueSqm / boothSqm);
                exhibition.setTotalAvailableBooths(availableBooths);
            } else {
                throw new RuntimeException("Cannot calculate booth capacity: venue space or booth size is invalid");
            }
            
            exhibitionRepository.save(exhibition);

            // We do NOT set venue.available = false here.
            // We rely on the date overlap check (above) to prevent double bookings.
            // venue.available is reserved for manual closures (e.g. maintenance).

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

        VenueRequest savedRequest = venueRequestRepository.save(request);

        return new VenueRequestResponse(
                savedRequest.getId(),
                savedRequest.getExhibition().getId(),
                savedRequest.getVenue().getId(),
                savedRequest.getVenue().getName(),
                savedRequest.getVenue().getAddress(),
                savedRequest.getStatus(),
                savedRequest.getOrgNotes(),
                savedRequest.getMunicipalityResponse(),
                savedRequest.getResponseDeadline(),
                savedRequest.getRequestedAt(),
                savedRequest.getReviewedAt(),
                savedRequest.getVenue().getMunicipalityId()
        );
    }

    // ----------------- Get All Municipalities -----------------
    public List<MunicipalityResponse> getAllMunicipalities() {
        return municipalityRepository.findAll().stream()
                .map(this::mapToMunicipalityResponse)
                .collect(Collectors.toList());
    }

    private MunicipalityResponse mapToMunicipalityResponse(Municipality municipality) {
        return new MunicipalityResponse(
                municipality.getId(),
                municipality.getName(),
                municipality.getRegion(),
                municipality.getContactEmail(),
                municipality.getContactPhone(),
                municipality.getOwner() != null ? municipality.getOwner().getId() : null
        );
    }
}
