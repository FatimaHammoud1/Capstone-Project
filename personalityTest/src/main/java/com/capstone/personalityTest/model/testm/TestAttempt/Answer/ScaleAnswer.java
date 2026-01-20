package com.capstone.personalityTest.model.testm.TestAttempt.Answer;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@DiscriminatorValue("SCALE")
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScaleAnswer extends Answer {

    private Integer scaleValue; // 1–7

    public String getLevel() {
        if (scaleValue >= 5) return "High";
        if (scaleValue >= 2) return "Medium";
        return "Low";
    }

}
