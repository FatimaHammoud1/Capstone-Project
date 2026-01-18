package com.capstone.personalityTest.model.Exhibition;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExhibitionFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long exhibitionId;

    private Long studentId;

    private Integer rating;

    @Column(columnDefinition = "TEXT")
    private String comments;

    private LocalDateTime createdAt;
}
