package com.capstone.personalityTest.service.financial_aid;

import com.capstone.personalityTest.dto.RequestDTO.financial_aid.FinancialAidApplyRequest;
import com.capstone.personalityTest.dto.RequestDTO.financial_aid.FinancialAidReviewRequest;
import com.capstone.personalityTest.dto.ResponseDTO.financial_aid.DonorResponse;
import com.capstone.personalityTest.dto.ResponseDTO.financial_aid.FinancialAidResponse;
import com.capstone.personalityTest.model.Exhibition.Organization;
import com.capstone.personalityTest.model.UserInfo;
import com.capstone.personalityTest.model.financial_aid.Donor;
import com.capstone.personalityTest.model.financial_aid.FinancialAidRequest;
import com.capstone.personalityTest.repository.Exhibition.OrganizationRepository;
import com.capstone.personalityTest.repository.UserInfoRepository;
import com.capstone.personalityTest.repository.financial_aid.DonorRepository;
import com.capstone.personalityTest.repository.financial_aid.FinancialAidRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FinancialAidService {

    private final FinancialAidRepository financialAidRepository;
    private final UserInfoRepository userInfoRepository;
    private final OrganizationRepository organizationRepository;
    private final DonorRepository donorRepository;
    private final FileStorageService fileStorageService;

    /**
     * Request financial aid with file uploads (ALL VALIDATION HERE)
     */
    @Transactional
    public FinancialAidResponse requestFinancialAidWithFiles(
            FinancialAidApplyRequest request,
            MultipartFile idCard,
            MultipartFile universityFees,
            MultipartFile gradeProof,
            String userEmail
    ) {
        // 1. Validate user exists
        UserInfo student = userInfoRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Validate organization exists
        Organization organization = organizationRepository.findById(request.getOrganizationId())
                .orElseThrow(() -> new RuntimeException("Organization not found"));

        // 3. Validate files are not empty
        if (idCard == null || idCard.isEmpty()) {
            throw new RuntimeException("ID card file is required");
        }
        if (universityFees == null || universityFees.isEmpty()) {
            throw new RuntimeException("University fees document is required");
        }
        if (gradeProof == null || gradeProof.isEmpty()) {
            throw new RuntimeException("Grade proof document is required");
        }

        // 4. Validate file types (accept only images and PDFs)
        validateFileType(idCard, "ID card");
        validateFileType(universityFees, "University fees");
        validateFileType(gradeProof, "Grade proof");

        // 5. Validate file sizes (max 5MB per file)
        validateFileSize(idCard, "ID card");
        validateFileSize(universityFees, "University fees");
        validateFileSize(gradeProof, "Grade proof");

        // 6. Store files and get filenames
        String studentId = student.getId().toString();
        String idCardFileName = fileStorageService.storeFile(idCard, studentId, "ID");
        String feesFileName = fileStorageService.storeFile(universityFees, studentId, "FEES");
        String gradesFileName = fileStorageService.storeFile(gradeProof, studentId, "GRADES");

        // 7. Create financial aid request entity
        FinancialAidRequest aidRequest = new FinancialAidRequest();
        aidRequest.setStudent(student);
        aidRequest.setOrganization(organization);
        aidRequest.setStudentName(request.getStudentName());
        aidRequest.setStudentPhone(request.getStudentPhone());
        aidRequest.setRequestedAmount(request.getRequestedAmount());
        aidRequest.setGpa(request.getGpa());
        aidRequest.setFieldOfStudy(request.getFieldOfStudy());
        aidRequest.setUniversityName(request.getUniversityName());
        aidRequest.setFamilyIncome(request.getFamilyIncome());
        aidRequest.setIdCardFileName(idCardFileName);
        aidRequest.setUniversityFeesFileName(feesFileName);
        aidRequest.setGradeProofFileName(gradesFileName);
        
        aidRequest.setReason(request.getReason());
        aidRequest.setStatus(FinancialAidRequest.Status.PENDING);
        aidRequest.setRequestedAt(LocalDateTime.now());

        // 8. Save request
        FinancialAidRequest savedRequest = financialAidRepository.save(aidRequest);

        // 9. Return response
        return mapToDetailResponse(savedRequest);
    }

    /**
     * Original method for backward compatibility (if needed)
     */
    @Transactional
    public FinancialAidResponse requestFinancialAid(FinancialAidApplyRequest request, String userEmail) {
        UserInfo student = userInfoRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Organization organization = organizationRepository.findById(request.getOrganizationId())
                .orElseThrow(() -> new RuntimeException("Organization not found"));

        FinancialAidRequest aidRequest = new FinancialAidRequest();
        aidRequest.setStudent(student);
        aidRequest.setOrganization(organization);
        aidRequest.setStudentName(request.getStudentName());
        aidRequest.setStudentPhone(request.getStudentPhone());
        aidRequest.setRequestedAmount(request.getRequestedAmount());
        aidRequest.setGpa(request.getGpa());
        aidRequest.setFieldOfStudy(request.getFieldOfStudy());
        aidRequest.setUniversityName(request.getUniversityName());
        aidRequest.setFamilyIncome(request.getFamilyIncome());
        aidRequest.setIdCardFileName(request.getIdCardFileName());
        aidRequest.setUniversityFeesFileName(request.getUniversityFeesFileName());
        aidRequest.setGradeProofFileName(request.getGradeProofFileName());
        aidRequest.setReason(request.getReason());

        aidRequest.setStatus(FinancialAidRequest.Status.PENDING);
        aidRequest.setRequestedAt(LocalDateTime.now());

        FinancialAidRequest savedRequest = financialAidRepository.save(aidRequest);

        return mapToDetailResponse(savedRequest);
    }

    public List<FinancialAidResponse> getStudentRequests(String userEmail) {
        UserInfo student = userInfoRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return financialAidRepository.findByStudentId(student.getId()).stream()
                .map(this::mapToDetailResponse)
                .collect(Collectors.toList());
    }

    public FinancialAidResponse getRequestDetails(Long requestId, String userEmail) {
        FinancialAidRequest request = financialAidRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        UserInfo student = userInfoRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!request.getStudent().getId().equals(student.getId())) {
            throw new RuntimeException("Access denied: You can only view your own requests.");
        }

        return mapToDetailResponse(request);
    }

    @Transactional
    public FinancialAidResponse cancelRequest(Long requestId, String userEmail) {
        // 1. Validate request exists
        FinancialAidRequest request = financialAidRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        // 2. Validate user exists
        UserInfo user = userInfoRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 3. Validate authorization
        boolean isStudent = request.getStudent().getId().equals(user.getId());
        boolean isOrgOwner = request.getOrganization().getOwner().getId().equals(user.getId());

        if (!isStudent && !isOrgOwner) {
            throw new RuntimeException("Access denied: You are not authorized to cancel this request.");
        }

        // 4. Validate status
        if (request.getStatus() == FinancialAidRequest.Status.DISBURSED) {
            throw new RuntimeException("Cannot cancel a disbursed request.");
        }

        if (request.getStatus() == FinancialAidRequest.Status.REJECTED) {
            throw new RuntimeException("Cannot cancel a rejected request.");
        }

        if (request.getStatus() == FinancialAidRequest.Status.CANCELLED) {
            throw new RuntimeException("Request is already cancelled.");
        }

        // 5. Return budget if was approved
        if (request.getStatus() == FinancialAidRequest.Status.APPROVED &&
                request.getApprovedAmount() != null &&
                request.getDonor() != null) {
            Donor donor = request.getDonor();
            donor.setAvailableBudget(donor.getAvailableBudget().add(request.getApprovedAmount()));
            donorRepository.save(donor);
        }

        // 6. Delete associated files from storage
        fileStorageService.deleteFile(request.getIdCardFileName());
        fileStorageService.deleteFile(request.getUniversityFeesFileName());
        fileStorageService.deleteFile(request.getGradeProofFileName());

        // 7. Cancel request
        request.setStatus(FinancialAidRequest.Status.CANCELLED);
        FinancialAidRequest savedRequest = financialAidRepository.save(request);

        return mapToDetailResponse(savedRequest);
    }

    public Map<String, Object> getPendingRequestsForOrganization(String userEmail) {
        UserInfo owner = userInfoRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Organization organization = organizationRepository.findByOwnerId(owner.getId())
                .orElseThrow(() -> new RuntimeException("Organization not found for this user"));

        List<FinancialAidRequest> pendingRequests = financialAidRepository.findByOrganizationIdAndStatus(
                organization.getId(), FinancialAidRequest.Status.PENDING);

        List<FinancialAidResponse> requestDtos = pendingRequests.stream()
                .map(this::mapToDetailResponse)
                .collect(Collectors.toList());

        List<Donor> donors = donorRepository.findByOrganizationId(organization.getId());
        BigDecimal totalAvailableBudget = donors.stream()
                .filter(d -> Boolean.TRUE.equals(d.getActive()))
                .map(Donor::getAvailableBudget)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> response = new HashMap<>();
        response.put("requests", requestDtos);
        response.put("availableBudget", totalAvailableBudget);

        return response;
    }

    public Map<String, Object> getAllRequestsForOrganization(String userEmail, FinancialAidRequest.Status status) {
        UserInfo owner = userInfoRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Organization organization = organizationRepository.findByOwnerId(owner.getId())
                .orElseThrow(() -> new RuntimeException("Organization not found for this user"));

        List<FinancialAidRequest> requests;
        if (status != null) {
            requests = financialAidRepository.findByOrganizationIdAndStatus(organization.getId(), status);
        } else {
            requests = financialAidRepository.findByOrganizationId(organization.getId());
        }

        List<FinancialAidResponse> requestDtos = requests.stream()
                .map(this::mapToDetailResponse)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("requests", requestDtos);

        return response;
    }

    @Transactional
    public FinancialAidResponse reviewRequest(Long requestId, FinancialAidReviewRequest reviewRequest, String userEmail) {
        // 1. Validate user exists
        UserInfo owner = userInfoRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Validate request exists
        FinancialAidRequest request = financialAidRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        // 3. Validate authorization
        if (!request.getOrganization().getOwner().getId().equals(owner.getId())) {
            throw new RuntimeException("Access denied: You are not the owner of this organization.");
        }

        // 4. Validate request status
        if (request.getStatus() != FinancialAidRequest.Status.PENDING) {
            throw new RuntimeException("Request has already been processed (Status: " + request.getStatus() + ")");
        }

        // 5. Process decision
        if ("APPROVE".equalsIgnoreCase(reviewRequest.getDecision())) {
            // Validate approved amount
            if (reviewRequest.getApprovedAmount() == null || reviewRequest.getApprovedAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("Approved amount must be greater than 0");
            }

            // Find a donor with enough budget
            List<Donor> donors = donorRepository.findByOrganizationId(request.getOrganization().getId());
            Donor selectedDonor = donors.stream()
                    .filter(d -> Boolean.TRUE.equals(d.getActive()) &&
                            d.getAvailableBudget().compareTo(reviewRequest.getApprovedAmount()) >= 0)
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Insufficient donor budget to approve this request."));

            // Deduct budget
            selectedDonor.setAvailableBudget(selectedDonor.getAvailableBudget().subtract(reviewRequest.getApprovedAmount()));
            donorRepository.save(selectedDonor);

            // Update Request
            request.setStatus(FinancialAidRequest.Status.APPROVED);
            request.setApprovedAmount(reviewRequest.getApprovedAmount());
            request.setDonor(selectedDonor);

        } else if ("REJECT".equalsIgnoreCase(reviewRequest.getDecision())) {
            // Validate rejection reason
            if (reviewRequest.getRejectionReason() == null || reviewRequest.getRejectionReason().isBlank()) {
                throw new RuntimeException("Rejection reason is required");
            }
            request.setStatus(FinancialAidRequest.Status.REJECTED);
            request.setRejectionReason(reviewRequest.getRejectionReason());
        } else {
            throw new RuntimeException("Invalid decision: " + reviewRequest.getDecision());
        }

        request.setReviewedAt(LocalDateTime.now());
        FinancialAidRequest savedRequest = financialAidRepository.save(request);

        return mapToDetailResponse(savedRequest);
    }

    @Transactional
    public FinancialAidResponse disburseAid(Long requestId, String userEmail) {
        // 1. Validate user exists
        UserInfo owner = userInfoRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Validate request exists
        FinancialAidRequest request = financialAidRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        // 3. Validate authorization
        if (!request.getOrganization().getOwner().getId().equals(owner.getId())) {
            throw new RuntimeException("Access denied: You are not the owner of this organization.");
        }

        // 4. Validate request status
        if (request.getStatus() != FinancialAidRequest.Status.APPROVED) {
            throw new RuntimeException("Cannot disburse aid. Request status must be APPROVED. Current status: " + request.getStatus());
        }

        // 5. Disburse aid
        request.setStatus(FinancialAidRequest.Status.DISBURSED);
        FinancialAidRequest savedRequest = financialAidRepository.save(request);

        return mapToDetailResponse(savedRequest);
    }

    public List<DonorResponse> getDonorsForOrganization(String userEmail) {
        UserInfo owner = userInfoRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Organization organization = organizationRepository.findByOwnerId(owner.getId())
                .orElseThrow(() -> new RuntimeException("Organization not found for this user"));

        return donorRepository.findByOrganizationId(organization.getId()).stream()
                .map(this::mapToDonorResponse)
                .collect(Collectors.toList());
    }

    public DonorResponse getDonorDetails(Long donorId, String userEmail) {
        UserInfo owner = userInfoRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Donor donor = donorRepository.findById(donorId)
                .orElseThrow(() -> new RuntimeException("Donor not found"));

        if (!donor.getOrganization().getOwner().getId().equals(owner.getId())) {
            throw new RuntimeException("Access denied: You are not the owner of this donor's organization.");
        }

        return mapToDonorDetailResponse(donor);
    }

    public Map<String, Object> getFinancialAidStatistics(String userEmail) {
        UserInfo owner = userInfoRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Organization organization = organizationRepository.findByOwnerId(owner.getId())
                .orElseThrow(() -> new RuntimeException("Organization not found for this user"));

        List<FinancialAidRequest> allRequests = financialAidRepository.findByOrganizationId(organization.getId());

        long totalRequests = allRequests.size();
        long pendingRequests = allRequests.stream().filter(r -> r.getStatus() == FinancialAidRequest.Status.PENDING).count();
        long approvedRequests = allRequests.stream().filter(r -> r.getStatus() == FinancialAidRequest.Status.APPROVED).count();
        long rejectedRequests = allRequests.stream().filter(r -> r.getStatus() == FinancialAidRequest.Status.REJECTED).count();
        long cancelledRequests = allRequests.stream().filter(r -> r.getStatus() == FinancialAidRequest.Status.CANCELLED).count();
        long disbursedRequests = allRequests.stream().filter(r -> r.getStatus() == FinancialAidRequest.Status.DISBURSED).count();

        BigDecimal totalAmountRequested = allRequests.stream()
                .map(FinancialAidRequest::getRequestedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalAmountApproved = allRequests.stream()
                .filter(r -> (r.getStatus() == FinancialAidRequest.Status.APPROVED ||
                        r.getStatus() == FinancialAidRequest.Status.DISBURSED)
                        && r.getApprovedAmount() != null)
                .map(FinancialAidRequest::getApprovedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalAmountDisbursed = allRequests.stream()
                .filter(r -> r.getStatus() == FinancialAidRequest.Status.DISBURSED && r.getApprovedAmount() != null)
                .map(FinancialAidRequest::getApprovedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Donor> donors = donorRepository.findByOrganizationId(organization.getId());
        BigDecimal availableBudget = donors.stream()
                .filter(d -> Boolean.TRUE.equals(d.getActive()))
                .map(Donor::getAvailableBudget)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long activeDonors = donors.stream().filter(d -> Boolean.TRUE.equals(d.getActive())).count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalRequests", totalRequests);
        stats.put("pendingRequests", pendingRequests);
        stats.put("approvedRequests", approvedRequests);
        stats.put("rejectedRequests", rejectedRequests);
        stats.put("cancelledRequests", cancelledRequests);
        stats.put("disbursedRequests", disbursedRequests);
        stats.put("totalAmountRequested", totalAmountRequested);
        stats.put("totalAmountApproved", totalAmountApproved);
        stats.put("totalAmountDisbursed", totalAmountDisbursed);
        stats.put("availableBudget", availableBudget);
        stats.put("activeDonors", activeDonors);

        return stats;
    }

    // ==================== Helper Methods ====================

    /**
     * Get user ID from email
     */
    public Long getUserIdFromEmail(String email) {
        UserInfo user = userInfoRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }

    /**
     * Validate file type (accept only images and PDFs)
     */
    private void validateFileType(MultipartFile file, String fieldName) {
        String contentType = file.getContentType();
        if (contentType == null) {
            throw new RuntimeException(fieldName + " file type could not be determined");
        }

        if (!contentType.startsWith("image/") && !contentType.equals("application/pdf")) {
            throw new RuntimeException(fieldName + " must be an image (JPG, PNG) or PDF file. Received: " + contentType);
        }
    }

    /**
     * Validate file size (max 5MB per file)
     */
    private void validateFileSize(MultipartFile file, String fieldName) {
        long maxSize = 5 * 1024 * 1024; // 5MB in bytes
        if (file.getSize() > maxSize) {
            throw new RuntimeException(fieldName + " file size must not exceed 5MB. Current size: " +
                    (file.getSize() / (1024 * 1024)) + "MB");
        }
    }

    /**
     * Map donor to response
     */
    private DonorResponse mapToDonorResponse(Donor donor) {
        DonorResponse response = new DonorResponse();
        response.setId(donor.getId());
        response.setName(donor.getName());
        response.setTotalBudget(donor.getTotalBudget());
        response.setAvailableBudget(donor.getAvailableBudget());
        response.setActive(donor.getActive());
        return response;
    }

    /**
     * Map donor to detailed response
     */
    private DonorResponse mapToDonorDetailResponse(Donor donor) {
        DonorResponse response = mapToDonorResponse(donor);
        BigDecimal amountDistributed = donor.getTotalBudget().subtract(donor.getAvailableBudget());
        response.setAmountDistributed(amountDistributed);

        long activeCount = financialAidRepository.findByOrganizationId(donor.getOrganization().getId()).stream()
                .filter(r -> r.getDonor() != null &&
                        r.getDonor().getId().equals(donor.getId()) &&
                        r.getStatus() == FinancialAidRequest.Status.APPROVED)
                .count();

        response.setActiveRequests(activeCount);

        return response;
    }

    /**
     * Map financial aid request to detailed response
     */
    private FinancialAidResponse mapToDetailResponse(FinancialAidRequest request) {
        FinancialAidResponse response = new FinancialAidResponse();
        response.setId(request.getId());
        response.setStudentId(request.getStudent().getId());
        response.setOrganizationId(request.getOrganization().getId());
        response.setOrganizationName(request.getOrganization().getName());
        response.setStudentName(request.getStudentName());
        response.setStudentPhone(request.getStudentPhone());
        response.setRequestedAmount(request.getRequestedAmount());
        response.setApprovedAmount(request.getApprovedAmount());
        response.setStatus(request.getStatus());
        response.setGpa(request.getGpa());
        response.setFieldOfStudy(request.getFieldOfStudy());
        response.setUniversityName(request.getUniversityName());
        response.setFamilyIncome(request.getFamilyIncome());

        // Generate file URLs
        // Note: Since migrating to Firebase, the 'FileName' fields now contain full secure URLs.
        FinancialAidResponse.Documents docs = new FinancialAidResponse.Documents();
        docs.setIdCard(request.getIdCardFileName());
        docs.setFees(request.getUniversityFeesFileName());
        docs.setGrades(request.getGradeProofFileName());
        response.setDocuments(docs);

        response.setReason(request.getReason());
        response.setRejectionReason(request.getRejectionReason());

        if (request.getDonor() != null) {
            response.setDonorId(request.getDonor().getId());
            response.setDonorName(request.getDonor().getName());
        }

        response.setRequestedAt(request.getRequestedAt());
        response.setReviewedAt(request.getReviewedAt());

        return response;
    }
}
















//package com.capstone.personalityTest.service.financial_aid;
//
//import com.capstone.personalityTest.dto.RequestDTO.financial_aid.FinancialAidApplyRequest;
//import com.capstone.personalityTest.dto.RequestDTO.financial_aid.FinancialAidReviewRequest;
//import com.capstone.personalityTest.dto.ResponseDTO.financial_aid.DonorResponse;
//import com.capstone.personalityTest.dto.ResponseDTO.financial_aid.FinancialAidResponse;
//
//import com.capstone.personalityTest.model.Exhibition.Organization;
//import com.capstone.personalityTest.model.UserInfo;
//import com.capstone.personalityTest.model.financial_aid.Donor;
//import com.capstone.personalityTest.model.financial_aid.FinancialAidRequest;
//import com.capstone.personalityTest.repository.Exhibition.OrganizationRepository;
//import com.capstone.personalityTest.repository.UserInfoRepository;
//import com.capstone.personalityTest.repository.financial_aid.DonorRepository;
//import com.capstone.personalityTest.repository.financial_aid.FinancialAidRepository;
//import jakarta.transaction.Transactional;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//public class FinancialAidService {
//
//    private final FinancialAidRepository financialAidRepository;
//    private final UserInfoRepository userInfoRepository;
//    private final OrganizationRepository organizationRepository;
//    private final DonorRepository donorRepository;
//
//    @Transactional
//    public FinancialAidResponse requestFinancialAid(FinancialAidApplyRequest request, String userEmail) {
//        UserInfo student = userInfoRepository.findByEmail(userEmail)
//                .orElseThrow(() -> new RuntimeException("User not found"));
//
//        Organization organization = organizationRepository.findById(request.getOrganizationId())
//                .orElseThrow(() -> new RuntimeException("Organization not found"));
//
//        FinancialAidRequest aidRequest = new FinancialAidRequest();
//        aidRequest.setStudent(student);
//        aidRequest.setOrganization(organization);
//        aidRequest.setStudentName(request.getStudentName());
//        aidRequest.setStudentPhone(request.getStudentPhone());
//        aidRequest.setRequestedAmount(request.getRequestedAmount());
//        aidRequest.setGpa(request.getGpa());
//        aidRequest.setFieldOfStudy(request.getFieldOfStudy());
//        aidRequest.setUniversityName(request.getUniversityName());
//        aidRequest.setFamilyIncome(request.getFamilyIncome());
//        aidRequest.setIdCardFileName(request.getIdCardFileName());
//        aidRequest.setUniversityFeesFileName(request.getUniversityFeesFileName());
//        aidRequest.setGradeProofFileName(request.getGradeProofFileName());
//        aidRequest.setReason(request.getReason());
//
//        aidRequest.setStatus(FinancialAidRequest.Status.PENDING);
//        aidRequest.setRequestedAt(LocalDateTime.now());
//
//        FinancialAidRequest savedRequest = financialAidRepository.save(aidRequest);
//
//        return mapToDetailResponse(savedRequest);
//    }
//
//    public List<FinancialAidResponse> getStudentRequests(String userEmail) {
//        UserInfo student = userInfoRepository.findByEmail(userEmail)
//                .orElseThrow(() -> new RuntimeException("User not found"));
//
//        return financialAidRepository.findByStudentId(student.getId()).stream()
//                .map(this::mapToDetailResponse)
//                .collect(Collectors.toList());
//    }
//
//    public FinancialAidResponse getRequestDetails(Long requestId, String userEmail) {
//        FinancialAidRequest request = financialAidRepository.findById(requestId)
//                .orElseThrow(() -> new RuntimeException("Request not found"));
//
//        UserInfo student = userInfoRepository.findByEmail(userEmail)
//                .orElseThrow(() -> new RuntimeException("User not found"));
//
//        if (!request.getStudent().getId().equals(student.getId())) {
//             throw new RuntimeException("Access denied: You can only view your own requests.");
//        }
//
//        return mapToDetailResponse(request);
//    }
//
//    @Transactional
//    public FinancialAidResponse cancelRequest(Long requestId, String userEmail) {
//        FinancialAidRequest request = financialAidRepository.findById(requestId)
//                .orElseThrow(() -> new RuntimeException("Request not found"));
//
//        UserInfo user = userInfoRepository.findByEmail(userEmail)
//                .orElseThrow(() -> new RuntimeException("User not found"));
//
//        boolean isStudent = request.getStudent().getId().equals(user.getId());
//        boolean isOrgOwner = request.getOrganization().getOwner().getId().equals(user.getId());
//
//        if (!isStudent && !isOrgOwner) {
//            throw new RuntimeException("Access denied: You are not authorized to cancel this request.");
//        }
//
//        if (request.getStatus() == FinancialAidRequest.Status.DISBURSED
//            || request.getStatus() == FinancialAidRequest.Status.REJECTED
//            || request.getStatus() == FinancialAidRequest.Status.CANCELLED) {
//             throw new RuntimeException("Cannot cancel request with status: " + request.getStatus());
//        }
//
//        if (request.getStatus() == FinancialAidRequest.Status.APPROVED && request.getApprovedAmount() != null && request.getDonor() != null) {
//            Donor donor = request.getDonor();
//            donor.setAvailableBudget(donor.getAvailableBudget().add(request.getApprovedAmount()));
//            donorRepository.save(donor);
//        }
//
//        request.setStatus(FinancialAidRequest.Status.CANCELLED);
//        FinancialAidRequest savedRequest = financialAidRepository.save(request);
//
//        return mapToDetailResponse(savedRequest);
//    }
//
//    public Map<String, Object> getPendingRequestsForOrganization(String userEmail) {
//        UserInfo owner = userInfoRepository.findByEmail(userEmail)
//                .orElseThrow(() -> new RuntimeException("User not found"));
//
//        Organization organization = organizationRepository.findByOwnerId(owner.getId())
//                .orElseThrow(() -> new RuntimeException("Organization not found for this user"));
//
//        List<FinancialAidRequest> pendingRequests = financialAidRepository.findByOrganizationIdAndStatus(
//                organization.getId(), FinancialAidRequest.Status.PENDING);
//
//        List<FinancialAidResponse> requestDtos = pendingRequests.stream()
//                .map(this::mapToDetailResponse)
//                .collect(Collectors.toList());
//
//        List<Donor> donors = donorRepository.findByOrganizationId(organization.getId());
//        BigDecimal totalAvailableBudget = donors.stream()
//                .filter(d -> Boolean.TRUE.equals(d.getActive()))
//                .map(Donor::getAvailableBudget)
//                .reduce(BigDecimal.ZERO, BigDecimal::add);
//
//        Map<String, Object> response = new HashMap<>();
//        response.put("requests", requestDtos);
//        response.put("availableBudget", totalAvailableBudget);
//
//        return response;
//    }
//
//    public Map<String, Object> getAllRequestsForOrganization(String userEmail, FinancialAidRequest.Status status) {
//        UserInfo owner = userInfoRepository.findByEmail(userEmail)
//                .orElseThrow(() -> new RuntimeException("User not found"));
//
//        Organization organization = organizationRepository.findByOwnerId(owner.getId())
//                .orElseThrow(() -> new RuntimeException("Organization not found for this user"));
//
//        List<FinancialAidRequest> requests;
//        if (status != null) {
//            requests = financialAidRepository.findByOrganizationIdAndStatus(organization.getId(), status);
//        } else {
//            requests = financialAidRepository.findByOrganizationId(organization.getId());
//        }
//
//        List<FinancialAidResponse> requestDtos = requests.stream()
//                .map(this::mapToDetailResponse)
//                .collect(Collectors.toList());
//
//        Map<String, Object> response = new HashMap<>();
//        response.put("requests", requestDtos);
//
//        return response;
//    }
//
//    @Transactional
//    public FinancialAidResponse reviewRequest(Long requestId, FinancialAidReviewRequest reviewRequest, String userEmail) {
//        UserInfo owner = userInfoRepository.findByEmail(userEmail)
//                .orElseThrow(() -> new RuntimeException("User not found"));
//
//        FinancialAidRequest request = financialAidRepository.findById(requestId)
//                .orElseThrow(() -> new RuntimeException("Request not found"));
//
//        // Verify that the user is the owner of the organization
//        if (!request.getOrganization().getOwner().getId().equals(owner.getId())) {
//             throw new RuntimeException("Access denied: You are not the owner of this organization.");
//        }
//
//        if (request.getStatus() != FinancialAidRequest.Status.PENDING) {
//             throw new RuntimeException("Request has already been processed (Status: " + request.getStatus() + ")");
//        }
//
//        if ("APPROVE".equalsIgnoreCase(reviewRequest.getDecision())) {
//             if (reviewRequest.getApprovedAmount() == null || reviewRequest.getApprovedAmount().compareTo(BigDecimal.ZERO) <= 0) {
//                 throw new RuntimeException("Approved amount must be greater than 0");
//             }
//
//             // Find a donor with enough budget
//             List<Donor> donors = donorRepository.findByOrganizationId(request.getOrganization().getId());
//             Donor selectedDonor = donors.stream()
//                     .filter(d -> Boolean.TRUE.equals(d.getActive()) && d.getAvailableBudget().compareTo(reviewRequest.getApprovedAmount()) >= 0)
//                     .findFirst()
//                     .orElseThrow(() -> new RuntimeException("Insufficient donor budget to approve this request."));
//
//             // Deduct budget
//             selectedDonor.setAvailableBudget(selectedDonor.getAvailableBudget().subtract(reviewRequest.getApprovedAmount()));
//             donorRepository.save(selectedDonor);
//
//             // Update Request
//             request.setStatus(FinancialAidRequest.Status.APPROVED);
//             request.setApprovedAmount(reviewRequest.getApprovedAmount());
//             request.setDonor(selectedDonor);
//
//        } else if ("REJECT".equalsIgnoreCase(reviewRequest.getDecision())) {
//             if (reviewRequest.getRejectionReason() == null || reviewRequest.getRejectionReason().isBlank()) {
//                 throw new RuntimeException("Rejection reason is required");
//             }
//             request.setStatus(FinancialAidRequest.Status.REJECTED);
//             request.setRejectionReason(reviewRequest.getRejectionReason());
//        } else {
//            throw new RuntimeException("Invalid decision: " + reviewRequest.getDecision());
//        }
//
//        request.setReviewedAt(LocalDateTime.now());
//        FinancialAidRequest savedRequest = financialAidRepository.save(request);
//
//        // TODO: Notify student
//
//        return mapToDetailResponse(savedRequest);
//    }
//
//    @Transactional
//    public FinancialAidResponse disburseAid(Long requestId, String userEmail) {
//        UserInfo owner = userInfoRepository.findByEmail(userEmail)
//                .orElseThrow(() -> new RuntimeException("User not found"));
//
//        FinancialAidRequest request = financialAidRepository.findById(requestId)
//                .orElseThrow(() -> new RuntimeException("Request not found"));
//
//        // Verify that the user is the owner of the organization
//        if (!request.getOrganization().getOwner().getId().equals(owner.getId())) {
//             throw new RuntimeException("Access denied: You are not the owner of this organization.");
//        }
//
//        if (request.getStatus() != FinancialAidRequest.Status.APPROVED) {
//             throw new RuntimeException("Cannot disburse aid. Request status must be APPROVED. Current status: " + request.getStatus());
//        }
//
//        request.setStatus(FinancialAidRequest.Status.DISBURSED);
//        FinancialAidRequest savedRequest = financialAidRepository.save(request);
//
//        return mapToDetailResponse(savedRequest);
//    }
//
//    public List<DonorResponse> getDonorsForOrganization(String userEmail) {
//        UserInfo owner = userInfoRepository.findByEmail(userEmail)
//                .orElseThrow(() -> new RuntimeException("User not found"));
//
//        Organization organization = organizationRepository.findByOwnerId(owner.getId())
//                .orElseThrow(() -> new RuntimeException("Organization not found for this user"));
//
//        return donorRepository.findByOrganizationId(organization.getId()).stream()
//                .map(this::mapToDonorResponse)
//                .collect(Collectors.toList());
//    }
//
//    public DonorResponse getDonorDetails(Long donorId, String userEmail) {
//        UserInfo owner = userInfoRepository.findByEmail(userEmail)
//                .orElseThrow(() -> new RuntimeException("User not found"));
//
//        Donor donor = donorRepository.findById(donorId)
//                .orElseThrow(() -> new RuntimeException("Donor not found"));
//
//        if (!donor.getOrganization().getOwner().getId().equals(owner.getId())) {
//            throw new RuntimeException("Access denied: You are not the owner of this donor's organization.");
//        }
//
//        return mapToDonorDetailResponse(donor);
//    }
//
//    public Map<String, Object> getFinancialAidStatistics(String userEmail) {
//        UserInfo owner = userInfoRepository.findByEmail(userEmail)
//                .orElseThrow(() -> new RuntimeException("User not found"));
//
//        Organization organization = organizationRepository.findByOwnerId(owner.getId())
//                .orElseThrow(() -> new RuntimeException("Organization not found for this user"));
//
//        List<FinancialAidRequest> allRequests = financialAidRepository.findByOrganizationId(organization.getId());
//
//        long totalRequests = allRequests.size();
//        long pendingRequests = allRequests.stream().filter(r -> r.getStatus() == FinancialAidRequest.Status.PENDING).count();
//        long approvedRequests = allRequests.stream().filter(r -> r.getStatus() == FinancialAidRequest.Status.APPROVED).count();
//        long rejectedRequests = allRequests.stream().filter(r -> r.getStatus() == FinancialAidRequest.Status.REJECTED).count();
//        long cancelledRequests = allRequests.stream().filter(r -> r.getStatus() == FinancialAidRequest.Status.CANCELLED).count();
//        // Disbursed is a separate status, but logically it's also "approved" in the past.
//        // The requirement separates them, so we will count DISBURSED separately.
//        long disbursedRequests = allRequests.stream().filter(r -> r.getStatus() == FinancialAidRequest.Status.DISBURSED).count();
//
//        // Total Requested Amount (Sum of all requests)
//        BigDecimal totalAmountRequested = allRequests.stream()
//                .map(FinancialAidRequest::getRequestedAmount)
//                .reduce(BigDecimal.ZERO, BigDecimal::add);
//
//        // Total Amount Approved (Sum of approvedAmount for APPROVED and DISBURSED requests)
//        BigDecimal totalAmountApproved = allRequests.stream()
//                .filter(r -> (r.getStatus() == FinancialAidRequest.Status.APPROVED || r.getStatus() == FinancialAidRequest.Status.DISBURSED)
//                        && r.getApprovedAmount() != null)
//                .map(FinancialAidRequest::getApprovedAmount)
//                .reduce(BigDecimal.ZERO, BigDecimal::add);
//
//        // Total Amount Disbursed (Sum of approvedAmount for DISBURSED requests only)
//        BigDecimal totalAmountDisbursed = allRequests.stream()
//                .filter(r -> r.getStatus() == FinancialAidRequest.Status.DISBURSED && r.getApprovedAmount() != null)
//                .map(FinancialAidRequest::getApprovedAmount)
//                .reduce(BigDecimal.ZERO, BigDecimal::add);
//
//        // Available Budget
//        List<Donor> donors = donorRepository.findByOrganizationId(organization.getId());
//        BigDecimal availableBudget = donors.stream()
//                .filter(d -> Boolean.TRUE.equals(d.getActive()))
//                .map(Donor::getAvailableBudget)
//                .reduce(BigDecimal.ZERO, BigDecimal::add);
//
//        long activeDonors = donors.stream().filter(d -> Boolean.TRUE.equals(d.getActive())).count();
//
//        Map<String, Object> stats = new HashMap<>();
//        stats.put("totalRequests", totalRequests);
//        stats.put("pendingRequests", pendingRequests);
//        stats.put("approvedRequests", approvedRequests);
//        stats.put("rejectedRequests", rejectedRequests);
//        stats.put("cancelledRequests", cancelledRequests);
//        stats.put("disbursedRequests", disbursedRequests); // Added for completeness
//        stats.put("totalAmountRequested", totalAmountRequested);
//        stats.put("totalAmountApproved", totalAmountApproved);
//        stats.put("totalAmountDisbursed", totalAmountDisbursed);
//        stats.put("availableBudget", availableBudget);
//        stats.put("activeDonors", activeDonors);
//
//        return stats;
//    }
//
//    private DonorResponse mapToDonorResponse(Donor donor) {
//        DonorResponse response = new DonorResponse();
//        response.setId(donor.getId());
//        response.setName(donor.getName());
//        response.setTotalBudget(donor.getTotalBudget());
//        response.setAvailableBudget(donor.getAvailableBudget());
//        response.setActive(donor.getActive());
//        return response;
//    }
//
//    private DonorResponse mapToDonorDetailResponse(Donor donor) {
//        DonorResponse response = mapToDonorResponse(donor);
//
//        BigDecimal amountDistributed = donor.getTotalBudget().subtract(donor.getAvailableBudget());
//        response.setAmountDistributed(amountDistributed);
//
//        // This is a simplified active requests count - roughly those approved but not yet disbursed/cancelled/rejected
//        // that are assigned to this donor.
//        // More precise logic might depend on how 'activeRequests' is defined (e.g., PENDING requests
//        // usually don't have a donor set yet).
//        // Let's assume active means "funds committed but not gone yet" -> APPROVED status.
//        // Actually, requests are assigned to a donor upon APPROVAL.
//        long activeCount = financialAidRepository.findByOrganizationId(donor.getOrganization().getId()).stream()
//                .filter(r -> r.getDonor() != null && r.getDonor().getId().equals(donor.getId())
//                        && r.getStatus() == FinancialAidRequest.Status.APPROVED)
//                .count();
//
//        response.setActiveRequests(activeCount);
//
//        return response;
//    }
//
//    private FinancialAidResponse mapToDetailResponse(FinancialAidRequest request) {
//        FinancialAidResponse response = new FinancialAidResponse();
//        response.setId(request.getId());
//        response.setStudentId(request.getStudent().getId());         // Mapping new field
//        response.setOrganizationId(request.getOrganization().getId()); // Mapping new field
//        response.setOrganizationName(request.getOrganization().getName()); // Mapping new field
//        response.setStudentName(request.getStudentName());
//        response.setStudentPhone(request.getStudentPhone());
//        response.setRequestedAmount(request.getRequestedAmount());
//        response.setApprovedAmount(request.getApprovedAmount());
//        response.setStatus(request.getStatus());
//        response.setGpa(request.getGpa());
//        response.setFieldOfStudy(request.getFieldOfStudy());
//        response.setUniversityName(request.getUniversityName());
//        response.setFamilyIncome(request.getFamilyIncome());
//
//        FinancialAidResponse.Documents docs = new FinancialAidResponse.Documents();
//        docs.setIdCard(request.getIdCardFileName());
//        docs.setFees(request.getUniversityFeesFileName());
//        docs.setGrades(request.getGradeProofFileName());
//        response.setDocuments(docs);
//
//        response.setReason(request.getReason());
//        response.setRejectionReason(request.getRejectionReason());
//
//        if (request.getDonor() != null) {
//            response.setDonorId(request.getDonor().getId());
//            response.setDonorName(request.getDonor().getName());
//        }
//
//        response.setRequestedAt(request.getRequestedAt());
//        response.setReviewedAt(request.getReviewedAt());
//
//        return response;
//    }
//}
