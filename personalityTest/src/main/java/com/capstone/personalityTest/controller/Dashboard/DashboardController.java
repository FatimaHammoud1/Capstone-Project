package com.capstone.personalityTest.controller.Dashboard;

import com.capstone.personalityTest.dto.ResponseDTO.Dashboard.ExhibitionOverviewResponse;
import com.capstone.personalityTest.service.Exhibition.ExhibitionDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final ExhibitionDashboardService exhibitionDashboardService;

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
        
        ExhibitionOverviewResponse overview = exhibitionDashboardService.getExhibitionsOverview(orgId);
        return ResponseEntity.ok(overview);
    }
}
