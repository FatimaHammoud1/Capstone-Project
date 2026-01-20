package com.capstone.personalityTest.repository.Exhibition;

import com.capstone.personalityTest.model.Exhibition.ExhibitionFinancial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExhibitionFinancialRepository extends JpaRepository<ExhibitionFinancial, Long> {
    java.util.Optional<ExhibitionFinancial> findByExhibitionId(Long exhibitionId);
}
