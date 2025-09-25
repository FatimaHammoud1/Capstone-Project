package com.capstone.personalityTest.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PersonalityResult {

    private String firstTrait;   // Highest score, e.g., "Investigative"
    private String secondTrait;  // Second highest, e.g., "Social"
    private String thirdTrait;   // Third highest, e.g., "Artistic"


    @ElementCollection
    @CollectionTable(
            name = "personality_result_scores",
            joinColumns = @JoinColumn(name = "test_attempt_id")
    )
    @MapKeyColumn(name = "trait")   // <-- KEY
    @Column(name = "score")         // <-- VALUE
    private Map<String, Integer> traitScores;

    // e.g., { "R"=5, "I"=7, "C"=3, "S"=4, "E"=6, "A"=2 }

    // Helper to compute top 3 traits automatically
    public void calculateTopTraits() {
        List<String> topTraits = traitScores.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(3)
                .map(Map.Entry::getKey)
                .toList();

        firstTrait = !topTraits.isEmpty() ? topTraits.get(0) : null;
        secondTrait = topTraits.size() > 1 ? topTraits.get(1) : null;
        thirdTrait = topTraits.size() > 2 ? topTraits.get(2) : null;
    }

    @Override
    public String toString() {
        return String.join("-",
                firstTrait != null ? firstTrait : "",
                secondTrait != null ? secondTrait : "",
                thirdTrait != null ? thirdTrait : "");
    }
}
