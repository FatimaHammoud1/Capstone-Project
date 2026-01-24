package com.capstone.personalityTest.service.financial_aid;

import com.capstone.personalityTest.dto.RequestDTO.financial_aid.FinancialAidApplyRequest;
import com.capstone.personalityTest.dto.RequestDTO.financial_aid.FinancialAidReviewRequest;
import com.capstone.personalityTest.dto.ResponseDTO.financial_aid.FinancialAidResponse;
import com.capstone.personalityTest.model.Exhibition.Organization;
import com.capstone.personalityTest.model.UserInfo;
import com.capstone.personalityTest.model.financial_aid.Donor;
import com.capstone.personalityTest.model.financial_aid.FinancialAidRequest;
import com.capstone.personalityTest.repository.Exhibition.OrganizationRepository;
import com.capstone.personalityTest.repository.UserInfoRepository;
import com.capstone.personalityTest.repository.financial_aid.DonorRepository;
import com.capstone.personalityTest.repository.financial_aid.FinancialAidRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Unit tests for FinancialAidService
 * Tests critical business logic: aid request creation, approval flow, and budget management
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("FinancialAidService Unit Tests")
@Transactional
@Rollback
class FinancialAidServiceTest {

    @Mock
    private FinancialAidRepository financialAidRepository;

    @Mock
    private UserInfoRepository userInfoRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private DonorRepository donorRepository;

    @InjectMocks
    private FinancialAidService financialAidService;

    private UserInfo testStudent;
    private UserInfo testOwner;
    private Organization testOrganization;
    private FinancialAidRequest testRequest;
    private Donor testDonor;

    @BeforeEach
    void setUp() {
        // Setup test student
        testStudent = new UserInfo();
        testStudent.setId(1L);
        testStudent.setEmail("student@test.com");
        testStudent.setName("Test Student");

        // Setup organization owner
        testOwner = new UserInfo();
        testOwner.setId(2L);
        testOwner.setEmail("owner@test.com");
        testOwner.setName("Test Owner");

        // Setup organization
        testOrganization = new Organization();
        testOrganization.setId(1L);
        testOrganization.setName("Test Organization");
        testOrganization.setOwner(testOwner);

        // Setup donor with budget
        testDonor = new Donor();
        testDonor.setId(1L);
        testDonor.setName("Test Donor");
        testDonor.setOrganization(testOrganization);
        testDonor.setAvailableBudget(new BigDecimal("10000.00"));
        testDonor.setActive(true);

        // Setup financial aid request
        testRequest = new FinancialAidRequest();
        testRequest.setId(1L);
        testRequest.setStudent(testStudent);
        testRequest.setOrganization(testOrganization);
        testRequest.setStudentName("Test Student");
        testRequest.setStudentPhone("1234567890");
        testRequest.setRequestedAmount(new BigDecimal("5000.00"));
        testRequest.setGpa(3.5);
        testRequest.setFieldOfStudy("Computer Science");
        testRequest.setUniversityName("Test University");
        testRequest.setFamilyIncome(new BigDecimal("2000.00"));
        testRequest.setIdCardUrl("http://example.com/id.jpg");
        testRequest.setUniversityFeesUrl("http://example.com/fees.pdf");
        testRequest.setGradeProofUrl("http://example.com/grades.pdf");
        testRequest.setReason("Need financial assistance for tuition");
        testRequest.setStatus(FinancialAidRequest.Status.PENDING);
        testRequest.setRequestedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should successfully create financial aid request")
    void testRequestFinancialAid_Success() {
        // Arrange
        FinancialAidApplyRequest applyRequest = new FinancialAidApplyRequest();
        applyRequest.setOrganizationId(1L);
        applyRequest.setStudentName("Test Student");
        applyRequest.setStudentPhone("1234567890");
        applyRequest.setRequestedAmount(new BigDecimal("5000.00"));
        applyRequest.setGpa(3.5);
        applyRequest.setFieldOfStudy("Computer Science");
        applyRequest.setUniversityName("Test University");
        applyRequest.setFamilyIncome(new BigDecimal("2000.00"));
        applyRequest.setIdCardUrl("http://example.com/id.jpg");
        applyRequest.setUniversityFeesUrl("http://example.com/fees.pdf");
        applyRequest.setGradeProofUrl("http://example.com/grades.pdf");
        applyRequest.setReason("Need financial assistance");

        when(userInfoRepository.findByEmail("student@test.com")).thenReturn(Optional.of(testStudent));
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(testOrganization));
        when(financialAidRepository.save(any(FinancialAidRequest.class))).thenReturn(testRequest);

        // Act
        FinancialAidResponse response = financialAidService.requestFinancialAid(applyRequest, "student@test.com");

        // Assert
        assertNotNull(response, "Response should not be null");
        
        // Verify repository interactions
        verify(userInfoRepository).findByEmail("student@test.com");
        verify(organizationRepository).findById(1L);
        verify(financialAidRepository).save(any(FinancialAidRequest.class));

        // Verify that the saved request has correct status
        ArgumentCaptor<FinancialAidRequest> requestCaptor = ArgumentCaptor.forClass(FinancialAidRequest.class);
        verify(financialAidRepository).save(requestCaptor.capture());
        FinancialAidRequest savedRequest = requestCaptor.getValue();
        
        assertEquals(FinancialAidRequest.Status.PENDING, savedRequest.getStatus());
        assertEquals(testStudent, savedRequest.getStudent());
        assertEquals(testOrganization, savedRequest.getOrganization());
        assertEquals(new BigDecimal("5000.00"), savedRequest.getRequestedAmount());
    }

    @Test
    @DisplayName("Should throw exception when student not found")
    void testRequestFinancialAid_StudentNotFound() {
        // Arrange
        FinancialAidApplyRequest applyRequest = new FinancialAidApplyRequest();
        when(userInfoRepository.findByEmail("nonexistent@test.com")).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> financialAidService.requestFinancialAid(applyRequest, "nonexistent@test.com"));
        
        assertEquals("User not found", exception.getMessage());
        verify(financialAidRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should successfully approve financial aid request with donor budget deduction")
    void testReviewRequest_Approve_Success() {
        // Arrange
        FinancialAidReviewRequest reviewRequest = new FinancialAidReviewRequest();
        reviewRequest.setDecision("APPROVE");
        reviewRequest.setApprovedAmount(new BigDecimal("4000.00"));

        when(userInfoRepository.findByEmail("owner@test.com")).thenReturn(Optional.of(testOwner));
        when(financialAidRepository.findById(1L)).thenReturn(Optional.of(testRequest));
        when(donorRepository.findByOrganizationId(1L)).thenReturn(Arrays.asList(testDonor));
        when(donorRepository.save(any(Donor.class))).thenReturn(testDonor);
        when(financialAidRepository.save(any(FinancialAidRequest.class))).thenReturn(testRequest);

        // Act
        FinancialAidResponse response = financialAidService.reviewRequest(1L, reviewRequest, "owner@test.com");

        // Assert
        assertNotNull(response);
        
        // Verify donor budget is deducted
        ArgumentCaptor<Donor> donorCaptor = ArgumentCaptor.forClass(Donor.class);
        verify(donorRepository).save(donorCaptor.capture());
        Donor savedDonor = donorCaptor.getValue();
        
        BigDecimal expectedBudget = new BigDecimal("6000.00"); // 10000 - 4000
        assertEquals(0, expectedBudget.compareTo(savedDonor.getAvailableBudget()), 
            "Donor budget should be reduced by approved amount");
        
        // Verify request is saved with approved status
        ArgumentCaptor<FinancialAidRequest> requestCaptor = ArgumentCaptor.forClass(FinancialAidRequest.class);
        verify(financialAidRepository).save(requestCaptor.capture());
        FinancialAidRequest savedRequest = requestCaptor.getValue();
        
        assertEquals(FinancialAidRequest.Status.APPROVED, savedRequest.getStatus());
        assertEquals(testDonor, savedRequest.getDonor());
        assertEquals(new BigDecimal("4000.00"), savedRequest.getApprovedAmount());
    }

    @Test
    @DisplayName("Should throw exception when approving without sufficient donor budget")
    void testReviewRequest_Approve_InsufficientBudget() {
        // Arrange
        FinancialAidReviewRequest reviewRequest = new FinancialAidReviewRequest();
        reviewRequest.setDecision("APPROVE");
        reviewRequest.setApprovedAmount(new BigDecimal("15000.00")); // More than donor budget

        testDonor.setAvailableBudget(new BigDecimal("10000.00"));

        when(userInfoRepository.findByEmail("owner@test.com")).thenReturn(Optional.of(testOwner));
        when(financialAidRepository.findById(1L)).thenReturn(Optional.of(testRequest));
        when(donorRepository.findByOrganizationId(1L)).thenReturn(Arrays.asList(testDonor));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> financialAidService.reviewRequest(1L, reviewRequest, "owner@test.com"));
        
        assertTrue(exception.getMessage().contains("donor") || exception.getMessage().contains("budget"), 
            "Exception message should mention donor or budget issue: " + exception.getMessage());
        verify(financialAidRepository, never()).save(any());
        verify(donorRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should successfully cancel approved request and refund donor budget")
    void testCancelRequest_RefundDonorBudget() {
        // Arrange
        testRequest.setStatus(FinancialAidRequest.Status.APPROVED);
        testRequest.setApprovedAmount(new BigDecimal("4000.00"));
        testRequest.setDonor(testDonor);
        testDonor.setAvailableBudget(new BigDecimal("6000.00")); // Already deducted

        when(financialAidRepository.findById(1L)).thenReturn(Optional.of(testRequest));
        when(userInfoRepository.findByEmail("student@test.com")).thenReturn(Optional.of(testStudent));
        when(donorRepository.save(any(Donor.class))).thenReturn(testDonor);
        when(financialAidRepository.save(any(FinancialAidRequest.class))).thenReturn(testRequest);

        // Act
        FinancialAidResponse response = financialAidService.cancelRequest(1L, "student@test.com");

        // Assert
        assertNotNull(response);
        
        // Verify budget is refunded
        ArgumentCaptor<Donor> donorCaptor = ArgumentCaptor.forClass(Donor.class);
        verify(donorRepository).save(donorCaptor.capture());
        Donor savedDonor = donorCaptor.getValue();
        
        BigDecimal expectedBudget = new BigDecimal("10000.00"); // 6000 + 4000 refund
        assertEquals(0, expectedBudget.compareTo(savedDonor.getAvailableBudget()), 
            "Donor budget should be refunded");
        
        // Verify request status is cancelled
        ArgumentCaptor<FinancialAidRequest> requestCaptor = ArgumentCaptor.forClass(FinancialAidRequest.class);
        verify(financialAidRepository).save(requestCaptor.capture());
        assertEquals(FinancialAidRequest.Status.CANCELLED, requestCaptor.getValue().getStatus());
    }

    @Test
    @DisplayName("Should throw exception when cancelling already disbursed request")
    void testCancelRequest_AlreadyDisbursed() {
        // Arrange
        testRequest.setStatus(FinancialAidRequest.Status.DISBURSED);

        when(financialAidRepository.findById(1L)).thenReturn(Optional.of(testRequest));
        when(userInfoRepository.findByEmail("student@test.com")).thenReturn(Optional.of(testStudent));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> financialAidService.cancelRequest(1L, "student@test.com"));
        
        assertTrue(exception.getMessage().contains("Cannot cancel request with status"));
        verify(financialAidRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when unauthorized user tries to cancel")
    void testCancelRequest_UnauthorizedUser() {
        // Arrange
        UserInfo unauthorizedUser = new UserInfo();
        unauthorizedUser.setId(999L);
        unauthorizedUser.setEmail("unauthorized@test.com");

        when(financialAidRepository.findById(1L)).thenReturn(Optional.of(testRequest));
        when(userInfoRepository.findByEmail("unauthorized@test.com")).thenReturn(Optional.of(unauthorizedUser));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> financialAidService.cancelRequest(1L, "unauthorized@test.com"));
        
        assertTrue(exception.getMessage().contains("Access denied"));
        verify(financialAidRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should get pending requests for organization with available budget calculation")
    void testGetPendingRequestsForOrganization_Success() {
        // Arrange
        Donor activeDonor1 = new Donor();
        activeDonor1.setAvailableBudget(new BigDecimal("5000.00"));
        activeDonor1.setActive(true);

        Donor activeDonor2 = new Donor();
        activeDonor2.setAvailableBudget(new BigDecimal("3000.00"));
        activeDonor2.setActive(true);

        Donor inactiveDonor = new Donor();
        inactiveDonor.setAvailableBudget(new BigDecimal("2000.00"));
        inactiveDonor.setActive(false);

        when(userInfoRepository.findByEmail("owner@test.com")).thenReturn(Optional.of(testOwner));
        when(organizationRepository.findByOwnerId(2L)).thenReturn(Optional.of(testOrganization));
        when(financialAidRepository.findByOrganizationIdAndStatus(1L, FinancialAidRequest.Status.PENDING))
            .thenReturn(Arrays.asList(testRequest));
        when(donorRepository.findByOrganizationId(1L))
            .thenReturn(Arrays.asList(activeDonor1, activeDonor2, inactiveDonor));

        // Act
        var response = financialAidService.getPendingRequestsForOrganization("owner@test.com");

        // Assert
        assertNotNull(response);
        assertTrue(response.containsKey("requests"));
        assertTrue(response.containsKey("availableBudget"));
        
        BigDecimal expectedBudget = new BigDecimal("8000.00"); // Only active donors: 5000 + 3000
        assertEquals(0, expectedBudget.compareTo((BigDecimal) response.get("availableBudget")), 
            "Should calculate total budget from active donors only");
        
        @SuppressWarnings("unchecked")
        List<FinancialAidResponse> requests = (List<FinancialAidResponse>) response.get("requests");
        assertFalse(requests.isEmpty(), "Should contain pending requests");
    }
}
