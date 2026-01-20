package com.capstone.personalityTest.service.financial_aid;

import com.capstone.personalityTest.dto.RequestDTO.FinancialAidApplyRequest;
import com.capstone.personalityTest.dto.ResponseDTO.FinancialAidDetailResponse;
import com.capstone.personalityTest.dto.ResponseDTO.FinancialAidResponse;
import com.capstone.personalityTest.model.Exhibition.Organization;
import com.capstone.personalityTest.model.UserInfo;
import com.capstone.personalityTest.model.financial_aid.FinancialAidRequest;
import com.capstone.personalityTest.repository.Exhibition.OrganizationRepository;
import com.capstone.personalityTest.repository.UserInfoRepository;
import com.capstone.personalityTest.repository.financial_aid.FinancialAidRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FinancialAidService {

    private final FinancialAidRepository financialAidRepository;
    private final UserInfoRepository userInfoRepository;
    private final OrganizationRepository organizationRepository;

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
        aidRequest.setIdCardUrl(request.getIdCardUrl());
        aidRequest.setUniversityFeesUrl(request.getUniversityFeesUrl());
        aidRequest.setGradeProofUrl(request.getGradeProofUrl());
        aidRequest.setReason(request.getReason());
        
        aidRequest.setStatus(FinancialAidRequest.Status.PENDING);
        aidRequest.setRequestedAt(LocalDateTime.now());

        FinancialAidRequest savedRequest = financialAidRepository.save(aidRequest);

        return mapToResponse(savedRequest);
    }

    public List<FinancialAidResponse> getStudentRequests(String userEmail) {
        UserInfo student = userInfoRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return financialAidRepository.findByStudentId(student.getId()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public FinancialAidDetailResponse getRequestDetails(Long requestId, String userEmail) {
        FinancialAidRequest request = financialAidRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));
        
        UserInfo student = userInfoRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!request.getStudent().getId().equals(student.getId())) {
             throw new RuntimeException("Access denied: You can only view your own requests.");
        }

        return mapToDetailResponse(request);
    }

    private FinancialAidResponse mapToResponse(FinancialAidRequest request) {
        FinancialAidResponse response = new FinancialAidResponse();
        response.setId(request.getId());
        response.setStudentId(request.getStudent().getId());
        response.setOrganizationId(request.getOrganization().getId());
        response.setOrganizationName(request.getOrganization().getName());
        response.setStudentName(request.getStudentName());
        response.setStatus(request.getStatus());
        response.setRequestedAmount(request.getRequestedAmount());
        response.setApprovedAmount(request.getApprovedAmount());
        response.setRequestedAt(request.getRequestedAt());
        response.setReviewedAt(request.getReviewedAt());
        return response;
    }

    private FinancialAidDetailResponse mapToDetailResponse(FinancialAidRequest request) {
        FinancialAidDetailResponse response = new FinancialAidDetailResponse();
        response.setId(request.getId());
        response.setStudentName(request.getStudentName());
        response.setRequestedAmount(request.getRequestedAmount());
        response.setApprovedAmount(request.getApprovedAmount());
        response.setStatus(request.getStatus());
        response.setGpa(request.getGpa());
        response.setUniversityName(request.getUniversityName());
        
        FinancialAidDetailResponse.Documents docs = new FinancialAidDetailResponse.Documents();
        docs.setIdCard(request.getIdCardUrl());
        docs.setFees(request.getUniversityFeesUrl());
        docs.setGrades(request.getGradeProofUrl());
        response.setDocuments(docs);
        
        response.setReason(request.getReason());
        response.setRequestedAt(request.getRequestedAt());
        response.setReviewedAt(request.getReviewedAt());
        
        return response;
    }
}
