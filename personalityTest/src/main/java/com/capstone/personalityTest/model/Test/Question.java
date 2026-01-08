package com.capstone.personalityTest.model.Test;

import com.capstone.personalityTest.model.Enum.AnswerType;
import com.capstone.personalityTest.model.Enum.TargetGender;
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
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String questionText;

    @Enumerated(EnumType.STRING)
    private AnswerType answerType;

    @Enumerated(EnumType.STRING)
    private TargetGender targetGender;

    @ManyToOne
    @JoinColumn(name = "section_id")
    private Section section;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SubQuestion> subQuestions = new ArrayList<>();

    public Question copy() {
        Question q = new Question();
        q.setQuestionText(this.questionText);
        q.setAnswerType(this.answerType);
        q.setTargetGender(this.targetGender);

        for (SubQuestion sq : this.subQuestions) {
            SubQuestion newSq = sq.copy();
            newSq.setQuestion(q);              // 🔴 parent reference
            q.getSubQuestions().add(newSq);
        }

        return q;
    }
}
