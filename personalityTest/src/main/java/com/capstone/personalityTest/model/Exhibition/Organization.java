package com.capstone.personalityTest.model.Exhibition;

import com.capstone.personalityTest.model.Enum.Exhibition.OrganizationType;
import com.capstone.personalityTest.model.UserInfo;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Organization {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private UserInfo owner;

    @Enumerated(EnumType.STRING)
    private OrganizationType type;

    private Boolean active;

    private LocalDateTime createdAt;
}
