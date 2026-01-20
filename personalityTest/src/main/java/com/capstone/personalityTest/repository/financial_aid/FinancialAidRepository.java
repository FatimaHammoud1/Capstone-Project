package com.capstone.personalityTest.repository.financial_aid;

import com.capstone.personalityTest.model.financial_aid.FinancialAidRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FinancialAidRepository extends JpaRepository<FinancialAidRequest, Long> {
    List<FinancialAidRequest> findByStudentId(Long studentId);
    List<FinancialAidRequest> findByOrganizationId(Long organizationId);
}
