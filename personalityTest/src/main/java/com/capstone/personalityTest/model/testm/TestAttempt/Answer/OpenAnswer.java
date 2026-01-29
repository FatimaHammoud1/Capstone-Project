package com.capstone.personalityTest.model.testm.TestAttempt.Answer;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@EqualsAndHashCode(callSuper = true) //includes answer fields
@DiscriminatorValue("OPEN")
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OpenAnswer extends Answer {

    @ElementCollection
    @CollectionTable(name = "open_answer_values", joinColumns = @JoinColumn(name = "open_answer_id"))
    @Column(name = "value")
    private List<String> values;

}

