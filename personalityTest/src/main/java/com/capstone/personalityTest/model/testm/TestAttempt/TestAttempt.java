package com.capstone.personalityTest.model.testm.TestAttempt;

import com.capstone.personalityTest.model.testm.EvaluationResult;
import com.capstone.personalityTest.model.testm.Test.Test;
import com.capstone.personalityTest.model.testm.TestAttempt.Answer.Answer;
import com.capstone.personalityTest.model.UserInfo;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private UserInfo student;

    @ManyToOne
    @JoinColumn(name = "test_id")
    private Test test;

    @OneToMany(mappedBy = "testAttempt", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Answer> answers = new ArrayList<>();

    @Embedded
    private EvaluationResult evaluationResult;

    private boolean finalized = false;
}