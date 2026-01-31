package com.capstone.personalityTest.service;

import com.capstone.personalityTest.dto.ResponseDTO.Dashboard.ExhibitionOverviewResponse;
import com.capstone.personalityTest.dto.ResponseDTO.Dashboard.FeedbackAnalyticsResponse;
import com.capstone.personalityTest.dto.ResponseDTO.Dashboard.FinancialAidAnalyticsResponse;
import com.capstone.personalityTest.dto.ResponseDTO.Dashboard.ParticipationStatsResponse;
import com.capstone.personalityTest.model.Enum.Exhibition.ActivityProviderRequestStatus;
import com.capstone.personalityTest.model.Enum.Exhibition.ExhibitionStatus;
import com.capstone.personalityTest.model.Enum.Exhibition.ParticipationStatus;
import com.capstone.personalityTest.model.Enum.Exhibition.StudentRegistrationStatus;
import com.capstone.personalityTest.model.Exhibition.*;
import com.capstone.personalityTest.model.financial_aid.FinancialAidRequest;
import com.capstone.personalityTest.repository.Exhibition.*;
import com.capstone.personalityTest.repository.financial_aid.FinancialAidRepository;
import com.capstone.personalityTest.repository.test.TestAttemptRepository;
import com.capstone.personalityTest.dto.ResponseDTO.Dashboard.TestAnalyticsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ExhibitionRepository exhibitionRepository;
    private final ExhibitionFinancialRepository exhibitionFinancialRepository;
    private final UniversityParticipationRepository universityParticipationRepository;
    private final SchoolParticipationRepository schoolParticipationRepository;
    private final StudentRegistrationRepository studentRegistrationRepository;
    private final ActivityProviderRequestRepository activityProviderRequestRepository;
    private final BoothRepository boothRepository;
    private final FinancialAidRepository financialAidRepository;
    private final ExhibitionFeedbackRepository exhibitionFeedbackRepository;
    private final TestAttemptRepository testAttemptRepository;

    /**
     * Get exhibition overview dashboard statistics
     * @param orgId Optional organization ID filter
     * @return ExhibitionOverviewResponse with aggregated statistics
     */
    public ExhibitionOverviewResponse getExhibitionsOverview(Long orgId) {
        // Fetch exhibitions (filtered by org if provided)
        List<Exhibition> exhibitions = orgId != null 
            ? exhibitionRepository.findByOrganizationId(orgId)
            : exhibitionRepository.findAll();

        // Calculate total exhibitions
        long totalExhibitions = exhibitions.size();

        // Count exhibitions by status
        long activeExhibitions = exhibitions.stream()
            .filter(e -> e.getStatus() == ExhibitionStatus.ACTIVE)
            .count();

        long planingExhibtion = exhibitions.stream()
            .filter(e -> e.getStatus() == ExhibitionStatus.PLANNING)
            .count();

        long completedExhibitions = exhibitions.stream()
            .filter(e -> e.getStatus() == ExhibitionStatus.COMPLETED)
            .count();

        long cancelledExhibitions = exhibitions.stream()
            .filter(e -> e.getStatus() == ExhibitionStatus.CANCELLED_BY_ORG || 
                         e.getStatus() == ExhibitionStatus.CANCELLED_BY_MUNICIPALITY)
            .count();

        // Create status breakdown map for all statuses
        Map<String, Long> statusBreakdown = new HashMap<>();
        for (ExhibitionStatus status : ExhibitionStatus.values()) {
            long count = exhibitions.stream()
                .filter(e -> e.getStatus() == status)
                .count();
            statusBreakdown.put(status.name(), count);
        }

        // Calculate financial totals from EXISTING financial records only
        // Note: Only exhibitions with calculated financials will be included
        // Org owners should call /api/exhibitions/{id}/calculate-financials first
        List<Long> exhibitionIds = exhibitions.stream()
            .map(Exhibition::getId)
            .collect(Collectors.toList());

        // Get all financial records for these exhibitions
        List<ExhibitionFinancial> financials = exhibitionFinancialRepository.findAll()
            .stream()
            .filter(f -> exhibitionIds.contains(f.getExhibition().getId()))
            .collect(Collectors.toList());

        // Sum up revenue and expenses
        BigDecimal totalRevenue = financials.stream()
            .map(ExhibitionFinancial::getTotalRevenue)
            .filter(revenue -> revenue != null)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpenses = financials.stream()
            .map(ExhibitionFinancial::getTotalExpenses)
            .filter(expenses -> expenses != null)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal netProfit = totalRevenue.subtract(totalExpenses);

        return new ExhibitionOverviewResponse(
            totalExhibitions,
            activeExhibitions,
            planingExhibtion,
            completedExhibitions,
            cancelledExhibitions,
            statusBreakdown,
            totalRevenue,
            totalExpenses,
            netProfit
        );
    }

    /**
     * Get participation statistics for a specific exhibition
     * @param exhibitionId Exhibition ID
     * @return ParticipationStatsResponse with detailed participation metrics
     */
    public ParticipationStatsResponse getParticipationStats(Long exhibitionId) {
        // Fetch exhibition
        Exhibition exhibition = exhibitionRepository.findById(exhibitionId)
            .orElseThrow(() -> new RuntimeException("Exhibition not found"));

        // ============ UNIVERSITY PARTICIPATION ============
        List<UniversityParticipation> uniParticipations = universityParticipationRepository
            .findByExhibitionId(exhibitionId);

        long uniInvited = uniParticipations.stream()
            .filter(p -> p.getStatus() == ParticipationStatus.INVITED)
            .count();

        long uniRegistered = uniParticipations.stream()
            .filter(p -> p.getStatus() == ParticipationStatus.REGISTERED)
            .count();

        long uniConfirmed = uniParticipations.stream()
            .filter(p -> p.getStatus() == ParticipationStatus.CONFIRMED)
            .count();

        long uniFinalized = uniParticipations.stream()
            .filter(p -> p.getStatus() == ParticipationStatus.FINALIZED)
            .count();

        long uniAttended = uniParticipations.stream()
            .filter(p -> p.getStatus() == ParticipationStatus.ATTENDED)
            .count();

        

        // Count booths allocated to universities
        int uniTotalBooths = (int) boothRepository.findByExhibitionId(exhibitionId).stream()
            .filter(booth -> booth.getUniversityParticipationId() != null)
            .count();

        // Calculate university attendance rate (attended / finalized)
        double uniAttendanceRate = uniFinalized > 0
            ? BigDecimal.valueOf(uniAttended)
                .divide(BigDecimal.valueOf(uniFinalized), 2, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue()
            : 0.0;

        ParticipationStatsResponse.UniversityStats universityStats = 
            new ParticipationStatsResponse.UniversityStats(
                uniInvited, uniRegistered, uniConfirmed, uniFinalized, uniAttended,
                uniTotalBooths, uniAttendanceRate
            );

        // ============ SCHOOL PARTICIPATION ============
        List<SchoolParticipation> schoolParticipations = schoolParticipationRepository
            .findByExhibitionId(exhibitionId);

        long schoolInvited = schoolParticipations.stream()
            .filter(p -> p.getStatus() == ParticipationStatus.INVITED)
            .count();

        long schoolRegistered = schoolParticipations.stream()
            .filter(p -> p.getStatus() == ParticipationStatus.REGISTERED)
            .count();

       

        long schoolFinalized = schoolParticipations.stream()
            .filter(p -> p.getStatus() == ParticipationStatus.FINALIZED)
            .count();

        long schoolAttended = schoolParticipations.stream()
            .filter(p -> p.getStatus() == ParticipationStatus.ATTENDED)
            .count();

       

        // Calculate school attendance rate (attended / finalized)
        double schoolAttendanceRate = schoolFinalized > 0
            ? BigDecimal.valueOf(schoolAttended)
                .divide(BigDecimal.valueOf(schoolFinalized), 2, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue()
            : 0.0;

        ParticipationStatsResponse.SchoolStats schoolStats = 
            new ParticipationStatsResponse.SchoolStats(
                schoolInvited, schoolRegistered, schoolFinalized, schoolAttended,
                 schoolAttendanceRate
            );

        // ============ STUDENT REGISTRATION ============
        List<StudentRegistration> studentRegistrations = studentRegistrationRepository
            .findByExhibitionId(exhibitionId);

        long studentsRegistered = studentRegistrations.stream()
            .filter(r -> r.getStatus() == StudentRegistrationStatus.REGISTERED )
            .count();

        long studentsAttended = studentRegistrations.stream()
            .filter(r -> r.getStatus() == StudentRegistrationStatus.ATTENDED)
            .count();

        long studentsNoShow = studentRegistrations.stream()
            .filter(r -> r.getStatus() == StudentRegistrationStatus.NO_SHOW)
            .count();

        // Calculate student attendance rate (attended / registered)
        double studentAttendanceRate = studentsRegistered > 0 
            ? BigDecimal.valueOf(studentsAttended)
                .divide(BigDecimal.valueOf(studentsRegistered), 2, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue()
            : 0.0;

        ParticipationStatsResponse.StudentStats studentStats = 
            new ParticipationStatsResponse.StudentStats(
                studentsRegistered, studentsAttended, studentsNoShow,
                studentAttendanceRate
            );

        // ============ ACTIVITY PROVIDER PARTICIPATION ============
        List<ActivityProviderRequest> providerRequests = activityProviderRequestRepository
            .findByExhibitionId(exhibitionId);

        long providersInvited = providerRequests.stream()
            .filter(r -> r.getStatus() == ActivityProviderRequestStatus.INVITED)
            .count();

        long providersProposed = providerRequests.stream()
            .filter(r -> r.getStatus() == ActivityProviderRequestStatus.PROPOSED)
            .count();

        long providersFinalized = providerRequests.stream()
            .filter(r -> r.getStatus() == ActivityProviderRequestStatus.FINALIZED)
            .count();

        long providersAttended = providerRequests.stream()
            .filter(r -> r.getStatus() == ActivityProviderRequestStatus.ATTENDED)
            .count();

        // Count booths allocated to activity providers
        int providerTotalBooths = (int) boothRepository.findByExhibitionId(exhibitionId).stream()
            .filter(booth -> booth.getActivityProviderRequestId() != null)
            .count();

        // Calculate activity provider attendance rate (attended / finalized)
        double providerAttendanceRate = providersFinalized > 0
            ? BigDecimal.valueOf(providersAttended)
                .divide(BigDecimal.valueOf(providersFinalized), 2, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue()
            : 0.0;

        ParticipationStatsResponse.ActivityProviderStats providerStats = 
            new ParticipationStatsResponse.ActivityProviderStats(
                providersInvited, providersProposed, providersFinalized,
                providersAttended, providerTotalBooths, providerAttendanceRate
            );

        // ============ OVERALL STATISTICS ============
        int totalExpectedVisitors = exhibition.getExpectedVisitors();
        
        // Calculate actual visitor count (people, not entities)
        // Sum visitor counts from attended universities + schools + individual students
        int actualVisitorsFromUniversities = uniParticipations.stream()
            .filter(p -> p.getStatus() == ParticipationStatus.ATTENDED && p.getExpectedVisitors() != null)
            .mapToInt(UniversityParticipation::getExpectedVisitors)
            .sum();
            
        int actualVisitorsFromSchools = schoolParticipations.stream()
            .filter(p -> p.getStatus() == ParticipationStatus.ATTENDED && p.getExpectedVisitors() != null)
            .mapToInt(SchoolParticipation::getExpectedVisitors)
            .sum();
        
        // Total actual attendees = visitors from universities + schools + individual students
        int actualAttendees = actualVisitorsFromUniversities + actualVisitorsFromSchools + (int) studentsAttended;

        double overallAttendanceRate = totalExpectedVisitors > 0
            ? BigDecimal.valueOf(actualAttendees)
                .divide(BigDecimal.valueOf(totalExpectedVisitors), 2, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue()
            : 0.0;

        return new ParticipationStatsResponse(
            exhibition.getId(),
            exhibition.getTitle(),
            universityStats,
            schoolStats,
            studentStats,
            providerStats,
            totalExpectedVisitors,
            actualAttendees,
            overallAttendanceRate
        );
    }

    /**
     * Get financial aid analytics
     * @param orgId Optional organization ID filter
     * @return FinancialAidAnalyticsResponse with requests grouped by university and major
     */
    public FinancialAidAnalyticsResponse getFinancialAidAnalytics(Long orgId) {
        // Fetch financial aid requests (filtered by org if provided)
        List<FinancialAidRequest> requests = orgId != null 
            ? financialAidRepository.findByOrganizationId(orgId)
            : financialAidRepository.findAll();

        // Total requests count
        long totalRequests = requests.size();

        // Group by university name
        Map<String, Long> requestsByUniversity = requests.stream()
            .collect(Collectors.groupingBy(
                FinancialAidRequest::getUniversityName,
                Collectors.counting()
            ));

        // Group by field of study (major)
        Map<String, Long> requestsByMajor = requests.stream()
            .collect(Collectors.groupingBy(
                FinancialAidRequest::getFieldOfStudy,
                Collectors.counting()
            ));

        // Group by status
        Map<String, Long> requestsByStatus = requests.stream()
            .collect(Collectors.groupingBy(
                request -> request.getStatus().name(),
                Collectors.counting()
            ));

        return new FinancialAidAnalyticsResponse(
            totalRequests,
            requestsByUniversity,
            requestsByMajor,
            requestsByStatus
        );
    }

    /**
     * Get feedback analytics for an exhibition
     * @param exhibitionId Exhibition ID
     * @return FeedbackAnalyticsResponse with feedback distribution by rating
     */
    public FeedbackAnalyticsResponse getFeedbackAnalytics(Long exhibitionId) {
        // Fetch all feedback for the exhibition
        List<ExhibitionFeedback> feedbacks = exhibitionFeedbackRepository.findByExhibitionId(exhibitionId);

        // Total feedback count
        long totalFeedbacks = feedbacks.size();

        // Calculate average rating
        double averageRating = feedbacks.isEmpty() ? 0.0 : feedbacks.stream()
            .mapToInt(ExhibitionFeedback::getRating)
            .average()
            .orElse(0.0);

        // Round to 2 decimal places
        averageRating = BigDecimal.valueOf(averageRating)
            .setScale(2, RoundingMode.HALF_UP)
            .doubleValue();

        // Group by rating (1-5)
        Map<Integer, Long> feedbacksByRating = feedbacks.stream()
            .collect(Collectors.groupingBy(
                ExhibitionFeedback::getRating,
                Collectors.counting()
            ));

        return new FeedbackAnalyticsResponse(
            totalFeedbacks,
            averageRating,
            feedbacksByRating
        );
    }

    /**
     * Get test analytics
     * @return TestAnalyticsResponse with attempts grouped by base test type
     */
    public TestAnalyticsResponse getTestAnalytics() {
        long totalAttempts = testAttemptRepository.count();
        List<Object[]> results = testAttemptRepository.countAttemptsByBaseTestType();
        
        Map<String, Long> attemptsByType = results.stream()
            .collect(Collectors.toMap(
                row -> (String) row[0],
                row -> ((Number) row[1]).longValue()
            ));

        return new TestAnalyticsResponse(totalAttempts, attemptsByType);
    }
}