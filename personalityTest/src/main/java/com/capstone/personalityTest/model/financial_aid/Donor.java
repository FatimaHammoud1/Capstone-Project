package com.capstone.personalityTest.model.financial_aid;

import com.capstone.personalityTest.model.Exhibition.Organization;
import com.capstone.personalityTest.model.UserInfo;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Donor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private UserInfo owner;

    private String name;

    private BigDecimal totalBudget;

    private BigDecimal availableBudget;

    private Boolean active;
}
