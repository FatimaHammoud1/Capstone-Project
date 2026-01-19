package com.capstone.personalityTest.model.Exhibition;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.capstone.personalityTest.model.UserInfo;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Municipality {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String region;

    private String contactEmail;

    private String contactPhone;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private UserInfo owner;
}
