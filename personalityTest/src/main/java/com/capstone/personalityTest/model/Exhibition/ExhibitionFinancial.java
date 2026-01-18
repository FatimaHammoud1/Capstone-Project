package com.capstone.personalityTest.model.Exhibition;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExhibitionFinancial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "exhibition_id")
    private Exhibition exhibition;

    private BigDecimal totalRevenue;

    private BigDecimal totalExpenses;

    private BigDecimal netProfit;

    private LocalDateTime calculatedAt;
}
