package com.capstone.personalityTest.service.Exhibition;

import com.capstone.personalityTest.dto.ResponseDTO.Exhibition.ExhibitionFinancialResponse;
import com.capstone.personalityTest.model.Enum.Exhibition.ActivityProviderRequestStatus;
import com.capstone.personalityTest.model.Enum.Exhibition.ParticipationStatus;
import com.capstone.personalityTest.model.Exhibition.*;
import com.capstone.personalityTest.repository.Exhibition.*;
import com.capstone.personalityTest.repository.UserInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExhibitionFinanceService {

    private final ExhibitionRepository exhibitionRepository;
    private final UniversityParticipationRepository universityParticipationRepository;
    private final SchoolParticipationRepository schoolParticipationRepository;
    private final ActivityProviderRequestRepository activityProviderRequestRepository;
    private final ExhibitionFinancialRepository exhibitionFinancialRepository;
    private final UserInfoRepository userInfoRepository;
    private final VenueRequestRepository venueRequestRepository;

    // ----------------- Calculate/Recalculate Financial Summary -----------------
    public ExhibitionFinancialResponse calculateFinancials(Long exhibitionId) {
        Exhibition exhibition = exhibitionRepository.findById(exhibitionId)
                .orElseThrow(() -> new RuntimeException("Exhibition not found"));

        // Get all universities for this exhibition
        List<UniversityParticipation> unis = universityParticipationRepository
                .findByExhibitionId(exhibitionId);

        // ----------------- Calculate Revenue -----------------
        BigDecimal totalRevenue = unis.stream()
                .filter(u -> u.getStatus() == ParticipationStatus.CONFIRMED)
                .map(UniversityParticipation::getParticipationFee)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // ----------------- Calculate Expenses -----------------
        // 1. Activity Provider Costs
        BigDecimal providerCosts = activityProviderRequestRepository
                .findByExhibitionIdAndStatus(exhibitionId, ActivityProviderRequestStatus.APPROVED)
                .stream()
                .map(ActivityProviderRequest::getTotalCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 2. Venue Rental Cost
        BigDecimal venueRentalCost = calculateVenueRentalCost(exhibition);

        // Total Expenses = Provider Costs + Venue Rental
        BigDecimal totalExpenses = providerCosts.add(venueRentalCost);

        // Check if financial record already exists (for recalculation)
        ExhibitionFinancial financial = exhibitionFinancialRepository
                .findByExhibitionId(exhibitionId)
                .orElse(new ExhibitionFinancial());

        financial.setExhibition(exhibition);
        financial.setTotalRevenue(totalRevenue);
        financial.setTotalExpenses(totalExpenses);
        financial.setNetProfit(totalRevenue.subtract(totalExpenses));
        financial.setCalculatedAt(LocalDateTime.now());

        ExhibitionFinancial saved = exhibitionFinancialRepository.save(financial);
        return mapToResponse(saved);
    }

    // ----------------- Calculate Venue Rental Cost -----------------
    private BigDecimal calculateVenueRentalCost(Exhibition exhibition) {
        // Find the approved venue request for this exhibition
        List<VenueRequest> venueRequests = venueRequestRepository.findByExhibitionId(exhibition.getId());
        
        VenueRequest approvedRequest = venueRequests.stream()
                .filter(vr -> vr.getStatus() == com.capstone.personalityTest.model.Enum.Exhibition.VenueRequestStatus.APPROVED)
                .findFirst()
                .orElse(null);

        if (approvedRequest == null || approvedRequest.getVenue() == null) {
            // No venue or venue not approved yet - return 0
            return BigDecimal.ZERO;
        }

        Venue venue = approvedRequest.getVenue();
        BigDecimal rentalFeePerDay = venue.getRentalFeePerDay();

        if (rentalFeePerDay == null) {
            // Venue has no rental fee set
            return BigDecimal.ZERO;
        }

        // Calculate number of days
        long numberOfDays = java.time.temporal.ChronoUnit.DAYS.between(
                exhibition.getStartDate(), 
                exhibition.getEndDate()
        ) + 1; // +1 to include both start and end dates

        // Total rental cost = rentalFeePerDay * numberOfDays
        return rentalFeePerDay.multiply(BigDecimal.valueOf(numberOfDays));
    }

    // ----------------- Get Financial Report -----------------
    public ExhibitionFinancialResponse getFinancialReport(Long exhibitionId) {
        ExhibitionFinancial financial = exhibitionFinancialRepository
                .findByExhibitionId(exhibitionId)
                .orElseThrow(() -> new RuntimeException("Financial report not found. Please calculate financials first."));
        
        return mapToResponse(financial);
    }

    // ----------------- Map to Response DTO -----------------
    private ExhibitionFinancialResponse mapToResponse(ExhibitionFinancial financial) {
        return new com.capstone.personalityTest.dto.ResponseDTO.Exhibition.ExhibitionFinancialResponse(
            financial.getId(),
            financial.getExhibition().getId(),
            financial.getTotalRevenue(),
            financial.getTotalExpenses(),
            financial.getNetProfit(),
            financial.getCalculatedAt()
        );
    }
}
