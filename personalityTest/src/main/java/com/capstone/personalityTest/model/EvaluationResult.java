package com.capstone.personalityTest.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationResult {

    // Top metrics by score (dynamic, not enum)
    @Column(name = "first_metric_code")
    private String firstMetric;

    @Column(name = "second_metric_code")
    private String secondMetric;

    @Column(name = "third_metric_code")
    private String thirdMetric;

    @ElementCollection
    @CollectionTable(
            name = "evaluation_scores",
            joinColumns = @JoinColumn(name = "test_attempt_id")
    )
    @MapKeyColumn(name = "metric_code")
    @Column(name = "score")
    private Map<String, Integer> metricScores;

    /**
     * Example:
     * metricScores = {
     *   "R"=5,
     *   "I"=7,
     *   "LOGIC"=10,
     *   "MEMORY"=6
     * }
     */
    public void calculateTopMetrics() {
        List<String> topMetrics = metricScores.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder()))
                .limit(3)
                .map(Map.Entry::getKey)
                .toList();

        firstMetric = topMetrics.size() > 0 ? topMetrics.get(0) : null;
        secondMetric = topMetrics.size() > 1 ? topMetrics.get(1) : null;
        thirdMetric = topMetrics.size() > 2 ? topMetrics.get(2) : null;
    }

    @Override
    public String toString() {
        return String.join("-",
                firstMetric != null ? firstMetric : "",
                secondMetric != null ? secondMetric : "",
                thirdMetric != null ? thirdMetric : "");
    }
}
