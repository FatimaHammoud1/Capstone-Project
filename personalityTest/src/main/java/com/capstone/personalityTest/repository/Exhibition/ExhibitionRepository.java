package com.capstone.personalityTest.repository.Exhibition;

import com.capstone.personalityTest.model.Exhibition.Exhibition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ExhibitionRepository extends JpaRepository<Exhibition, Long> {
    List<Exhibition> findByOrganizationId(Long organizationId);
}
