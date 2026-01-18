package com.capstone.personalityTest.model.Exhibition;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Venue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long municipalityId;

    private String name;

    private String address;

    private Integer maxCapacity;

    private Double spaceSqm;

    private BigDecimal rentalFeePerDay;

    private Boolean active;
}
