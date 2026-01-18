package com.capstone.personalityTest.repository.Exhibition;

import com.capstone.personalityTest.model.Exhibition.VenueRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface VenueRequestRepository extends JpaRepository<VenueRequest, Long> {
    List<VenueRequest> findByExhibitionId(Long exhibitionId);
}
