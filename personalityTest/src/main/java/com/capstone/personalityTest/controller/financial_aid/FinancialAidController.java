package com.capstone.personalityTest.controller.financial_aid;



import com.capstone.personalityTest.dto.RequestDTO.FinancialAidApplyRequest;
import com.capstone.personalityTest.dto.RequestDTO.FinancialAidReviewRequest;
import com.capstone.personalityTest.dto.ResponseDTO.FinancialAidResponse;

import com.capstone.personalityTest.model.financial_aid.FinancialAidRequest;
import com.capstone.personalityTest.service.financial_aid.FinancialAidService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/financial-aid")
@RequiredArgsConstructor
public class FinancialAidController {

    private final FinancialAidService financialAidService;

    @PostMapping("/request")
    @PreAuthorize("hasAnyRole('STUDENT', 'DEVELOPER')")
    public ResponseEntity<FinancialAidResponse> requestFinancialAid(
            @Valid @RequestBody FinancialAidApplyRequest request,
            Principal principal) {
        return ResponseEntity.ok(financialAidService.requestFinancialAid(request, principal.getName()));
    }

    @GetMapping("/my-requests")
    @PreAuthorize("hasAnyRole('STUDENT', 'DEVELOPER')")
    public ResponseEntity<Map<String, List<FinancialAidResponse>>>  getMyRequests(Principal principal) {
        List<FinancialAidResponse> requests = financialAidService.getStudentRequests(principal.getName());
        return ResponseEntity.ok(Map.of("requests", requests));
    }

    @GetMapping("/{requestId}")
    @PreAuthorize("hasAnyRole('STUDENT', 'DEVELOPER')")
    public ResponseEntity<FinancialAidResponse> getRequestDetails(
            @PathVariable Long requestId,
            Principal principal) {
        return ResponseEntity.ok(financialAidService.getRequestDetails(requestId, principal.getName()));
    }

    @PostMapping("/{requestId}/cancel")
    @PreAuthorize("hasAnyRole('STUDENT', 'DEVELOPER')")
    public ResponseEntity<FinancialAidResponse> cancelRequest(
            @PathVariable Long requestId,
            Principal principal) {
        return ResponseEntity.ok(financialAidService.cancelRequest(requestId, principal.getName()));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('ORG_OWNER', 'DEVELOPER')")
    public ResponseEntity<Map<String, Object>> getPendingRequests(Principal principal) {
        return ResponseEntity.ok(financialAidService.getPendingRequestsForOrganization(principal.getName()));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ORG_OWNER', 'DEVELOPER')")
    public ResponseEntity<Map<String, Object>> getAllRequests(
            @RequestParam(required = false) FinancialAidRequest.Status status,
            Principal principal) {
        return ResponseEntity.ok(financialAidService.getAllRequestsForOrganization(principal.getName(), status));
    }

    @PostMapping("/{requestId}/review")
    @PreAuthorize("hasAnyRole('ORG_OWNER', 'DEVELOPER')")
    public ResponseEntity<FinancialAidResponse> reviewRequest(
            @PathVariable Long requestId,
            @Valid @RequestBody FinancialAidReviewRequest reviewRequest,
            Principal principal) {
        return ResponseEntity.ok(financialAidService.reviewRequest(requestId, reviewRequest, principal.getName()));
    }

    @PostMapping("/{requestId}/disburse")
    @PreAuthorize("hasAnyRole('ORG_OWNER', 'DEVELOPER')")
    public ResponseEntity<FinancialAidResponse> disburseAid(
            @PathVariable Long requestId,
            Principal principal) {
        return ResponseEntity.ok(financialAidService.disburseAid(requestId, principal.getName()));
    }
}
