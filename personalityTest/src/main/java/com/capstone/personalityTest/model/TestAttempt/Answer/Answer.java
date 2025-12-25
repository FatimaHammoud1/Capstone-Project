package com.capstone.personalityTest.model.TestAttempt.Answer;

import com.capstone.personalityTest.model.Test.Question;
import com.capstone.personalityTest.model.Test.SubQuestion;
import com.capstone.personalityTest.model.TestAttempt.TestAttempt;
import jakarta.persistence.*;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.Data;

@Entity
@Data
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "answer_type")
public abstract class Answer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @ManyToOne
    @JoinColumn(name = "sub_question_id")
    private SubQuestion subQuestion;

    @ManyToOne(optional = false)
    @JoinColumn(name = "test_attempt_id", nullable = false)
    private TestAttempt testAttempt;
}