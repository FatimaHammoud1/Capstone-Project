package com.capstone.personalityTest.repository.financial_aid;

import com.capstone.personalityTest.model.financial_aid.Donor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DonorRepository extends JpaRepository<Donor, Long> {
    List<Donor> findByOrganizationId(Long organizationId);
    Optional<Donor> findByOwnerId(Long ownerId); // To find if a user is a donor
}
