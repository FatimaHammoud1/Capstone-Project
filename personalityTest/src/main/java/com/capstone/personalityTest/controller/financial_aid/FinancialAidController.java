package com.capstone.personalityTest.controller.financial_aid;

import com.capstone.personalityTest.dto.RequestDTO.financial_aid.FinancialAidApplyRequest;
import com.capstone.personalityTest.dto.RequestDTO.financial_aid.FinancialAidReviewRequest;
import com.capstone.personalityTest.dto.ResponseDTO.financial_aid.DonorResponse;
import com.capstone.personalityTest.dto.ResponseDTO.financial_aid.FinancialAidResponse;
import com.capstone.personalityTest.model.financial_aid.FinancialAidRequest;
import com.capstone.personalityTest.service.financial_aid.FileStorageService;
import com.capstone.personalityTest.service.financial_aid.FinancialAidService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/financial-aid")
@RequiredArgsConstructor
public class FinancialAidController {

    private final FinancialAidService financialAidService;

    private final FileStorageService fileStorageService;

    /**
     * Submit financial aid request with file uploads
     */
    @PostMapping(value = "/request", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('STUDENT', 'DEVELOPER')")
    @Operation(
            summary = "Submit financial aid request",
            description = "Student submits financial aid request with required documents (ID card, university fees, grade proof)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Request submitted successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid input or missing files"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized")
            }
    )
    public ResponseEntity<FinancialAidResponse> requestFinancialAid(
            @RequestParam("organizationId") Long organizationId,
            @RequestParam("studentName") String studentName,
            @RequestParam("studentPhone") String studentPhone,
            @RequestParam("requestedAmount") java.math.BigDecimal requestedAmount,
            @RequestParam("gpa") Double gpa,
            @RequestParam("fieldOfStudy") String fieldOfStudy,
            @RequestParam("universityName") String universityName,
            @RequestParam("familyIncome") java.math.BigDecimal familyIncome,
            @RequestParam("reason") String reason,
            @RequestParam("idCard") MultipartFile idCard,
            @RequestParam("universityFees") MultipartFile universityFees,
            @RequestParam("gradeProof") MultipartFile gradeProof,
            Principal principal
    ) {        
            FinancialAidApplyRequest request = new FinancialAidApplyRequest();
            request.setOrganizationId(organizationId);
            request.setStudentName(studentName);
            request.setStudentPhone(studentPhone);
            request.setRequestedAmount(requestedAmount);
            request.setGpa(gpa);
            request.setFieldOfStudy(fieldOfStudy);
            request.setUniversityName(universityName);
            request.setFamilyIncome(familyIncome);
            request.setReason(reason);

            FinancialAidResponse response = financialAidService.requestFinancialAidWithFiles(
                    request,
                    idCard,
                    universityFees,
                    gradeProof,
                    principal.getName()
            );
            return ResponseEntity.ok(response);
    }

    /**
     * Download/view uploaded document
     */
    @GetMapping("/files/{fileName}")
    @PreAuthorize("hasAnyRole('STUDENT', 'ORG_OWNER', 'DEVELOPER')")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable String fileName,
            Principal principal
    ) {
        Resource resource = fileStorageService.loadFileAsResource(fileName);

        String contentType = determineContentType(fileName);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
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
    @PreAuthorize("hasAnyRole('STUDENT','ORG_OWNER', 'DEVELOPER')")
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

    @GetMapping("/donors")
    @PreAuthorize("hasAnyRole('ORG_OWNER', 'DEVELOPER')")
    public ResponseEntity<Map<String, List<DonorResponse>>> getDonors(Principal principal) {
        List<DonorResponse> donors = financialAidService.getDonorsForOrganization(principal.getName());
        return ResponseEntity.ok(Map.of("donors", donors));
    }

    @GetMapping("/donors/{donorId}")
    @PreAuthorize("hasAnyRole('ORG_OWNER', 'DEVELOPER')")
    public ResponseEntity<DonorResponse> getDonor(
            @PathVariable Long donorId,
            Principal principal) {
        return ResponseEntity.ok(financialAidService.getDonorDetails(donorId, principal.getName()));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ORG_OWNER', 'DEVELOPER')")
    public ResponseEntity<Map<String, Object>> getStats(Principal principal) {
        return ResponseEntity.ok(financialAidService.getFinancialAidStatistics(principal.getName()));
    }

    /**
     * Determine content type from filename
     */
    private String determineContentType(String fileName) {
        String lowerFileName = fileName.toLowerCase();
        if (lowerFileName.endsWith(".pdf")) {
            return "application/pdf";
        } else if (lowerFileName.endsWith(".jpg") || lowerFileName.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (lowerFileName.endsWith(".png")) {
            return "image/png";
        }
        return "application/octet-stream";
    }
}
