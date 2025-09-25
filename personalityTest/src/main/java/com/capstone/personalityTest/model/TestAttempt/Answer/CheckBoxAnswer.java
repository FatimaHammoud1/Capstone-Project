package com.capstone.personalityTest.model.TestAttempt.Answer;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@DiscriminatorValue("BINARY")
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckBoxAnswer extends Answer {

    // 1 = YES/LIKE, 0 = NO/DISLIKE
    private boolean binaryValue;

}
