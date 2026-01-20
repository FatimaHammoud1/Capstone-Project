package com.capstone.personalityTest.model.Exhibition;

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
public class ExhibitionFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "exhibition_id")
    private Exhibition exhibition;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private UserInfo student;

    private Integer rating;

    @Column(columnDefinition = "TEXT")
    private String comments;

    private LocalDateTime createdAt;
}
