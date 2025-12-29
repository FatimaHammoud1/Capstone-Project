package com.capstone.personalityTest.model.Test;

import com.capstone.personalityTest.model.Enum.TargetGender;

import jakarta.persistence.*;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubQuestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String subQuestionText;

    @Enumerated(EnumType.STRING)
    private TargetGender targetGender;

    @ManyToOne
    @JoinColumn(name = "question_id")
    private Question question;

    @ManyToOne
    @JoinColumn(name = "metric_id")
    private Metric metric;


    public SubQuestion copy() {
        SubQuestion sq = new SubQuestion();
        sq.setSubQuestionText(this.subQuestionText);
        sq.setTargetGender(this.targetGender);
        sq.setMetric(this.metric);
        return sq;
    }
}
