package com.capstone.personalityTest.dto.ResponseDTO.Exhibition;

import com.capstone.personalityTest.model.Enum.Exhibition.VenueRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VenueRequestResponse {
    private Long id;
    private Long exhibitionId;
    private Long venueId;
    private String venueName;
    private String venueAddress;
    private VenueRequestStatus status;
    private String orgNotes;
    private String municipalityResponse;
    private LocalDateTime responseDeadline;
    private LocalDateTime requestedAt;
    private LocalDateTime reviewedAt;
    private Long municipalityId;
}
