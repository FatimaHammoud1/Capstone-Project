package com.capstone.personalityTest.controller.Dashboard;

import com.capstone.personalityTest.dto.ResponseDTO.Dashboard.ExhibitionOverviewResponse;
import com.capstone.personalityTest.dto.ResponseDTO.Dashboard.FeedbackAnalyticsResponse;
import com.capstone.personalityTest.dto.ResponseDTO.Dashboard.FinancialAidAnalyticsResponse;
import com.capstone.personalityTest.dto.ResponseDTO.Dashboard.ParticipationStatsResponse;
import com.capstone.personalityTest.dto.ResponseDTO.Dashboard.TestAnalyticsResponse;
import com.capstone.personalityTest.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * Get exhibition overview dashboard statistics
     * 
     * @param orgId Optional organization ID to filter exhibitions
     * @return Exhibition overview with status breakdown and financial summary
     */
    @GetMapping("/exhibitions/overview")
    @PreAuthorize("hasAnyRole('ORG_OWNER', 'DEVELOPER')")
    public ResponseEntity<ExhibitionOverviewResponse> getExhibitionsOverview(
            @RequestParam(required = false) Long orgId) {
        
        ExhibitionOverviewResponse overview = dashboardService.getExhibitionsOverview(orgId);
        return ResponseEntity.ok(overview);
    }

    /**
     * Get participation statistics for a specific exhibition
     * 
     * @param exhibitionId Exhibition ID
     * @return Detailed participation stats for universities, schools, students, and activity providers
     */
    @GetMapping("/exhibitions/{exhibitionId}/participation-stats")
    @PreAuthorize("hasAnyRole('ORG_OWNER', 'DEVELOPER')")
    public ResponseEntity<ParticipationStatsResponse> getParticipationStats(
            @PathVariable Long exhibitionId) {
        
        ParticipationStatsResponse stats = dashboardService.getParticipationStats(exhibitionId);
        return ResponseEntity.ok(stats);
    }

    /**
     * Get financial aid analytics
     * 
     * @param orgId Optional organization ID to filter financial aid requests
     * @return Financial aid analytics with requests grouped by university and major
     */
    @GetMapping("/financial-aid/analytics")
    @PreAuthorize("hasAnyRole('ORG_OWNER', 'DEVELOPER')")
    public ResponseEntity<FinancialAidAnalyticsResponse> getFinancialAidAnalytics(
            @RequestParam(required = false) Long orgId) {
        
        FinancialAidAnalyticsResponse analytics = dashboardService.getFinancialAidAnalytics(orgId);
        return ResponseEntity.ok(analytics);
    }

    /**
     * Get feedback analytics for a specific exhibition
     * 
     * @param exhibitionId Exhibition ID
     * @return Feedback analytics with distribution by rating
     */
    @GetMapping("/exhibitions/{exhibitionId}/feedback-analytics")
    @PreAuthorize("hasAnyRole('ORG_OWNER', 'DEVELOPER')")
    public ResponseEntity<FeedbackAnalyticsResponse> getFeedbackAnalytics(
            @PathVariable Long exhibitionId) {
        
        FeedbackAnalyticsResponse analytics = dashboardService.getFeedbackAnalytics(exhibitionId);
        return ResponseEntity.ok(analytics);
    }

    /**
     * Get test analytics (Global)
     * 
     * @return Test analytics grouped by base test type
     */
    @GetMapping("/test/analytics")
    @PreAuthorize("hasAnyRole('DEVELOPER', 'MUNICIPALITY_ADMIN')")
    public ResponseEntity<TestAnalyticsResponse> getTestAnalytics() {
        return ResponseEntity.ok(dashboardService.getTestAnalytics());
    }
}
