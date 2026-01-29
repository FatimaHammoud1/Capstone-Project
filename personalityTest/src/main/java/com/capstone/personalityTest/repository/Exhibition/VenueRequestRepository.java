package com.capstone.personalityTest.repository.Exhibition;

import com.capstone.personalityTest.model.Enum.Exhibition.VenueRequestStatus;
import com.capstone.personalityTest.model.Exhibition.VenueRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface VenueRequestRepository extends JpaRepository<VenueRequest, Long> {
    List<VenueRequest> findByExhibitionId(Long exhibitionId);
    List<VenueRequest> findByVenueIdAndStatus(Long venueId, VenueRequestStatus status);
}
