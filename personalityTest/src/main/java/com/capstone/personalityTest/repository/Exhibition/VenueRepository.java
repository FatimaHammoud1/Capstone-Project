package com.capstone.personalityTest.repository.Exhibition;


import com.capstone.personalityTest.model.Exhibition.Venue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VenueRepository extends JpaRepository<Venue, Long> {
    Optional<Venue> findByName(String name);
}

