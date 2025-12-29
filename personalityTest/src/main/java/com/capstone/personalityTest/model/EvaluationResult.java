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



//Great question 👍
//Let’s break down this syntax step by step.
//
//We have:
//
//```java
//List<PersonalityTrait> topTraits = traitScores.entrySet().stream()
//        .sorted(Map.Entry.<PersonalityTrait, Integer>comparingByValue(Comparator.reverseOrder()))
//        .limit(3)
//        .map(Map.Entry::getKey)
//        .toList();
//```
//
//---
//
//### 1. `traitScores.entrySet().stream()`
//
//* `traitScores` is a `Map<PersonalityTrait, Integer>`.
//* `entrySet()` returns a set of key-value pairs → `Set<Map.Entry<PersonalityTrait, Integer>>`.
//* `.stream()` turns that set into a **stream** so we can process it with functional operations (like filter, sort, map).
//
//So now we’re working with a stream of `Map.Entry<PersonalityTrait, Integer>` objects.
//Each entry has:
//
//* `getKey()` → the `PersonalityTrait`
//* `getValue()` → the score (Integer)
//
//---
//
//### 2. `.sorted(Map.Entry.<PersonalityTrait, Integer>comparingByValue(Comparator.reverseOrder()))`
//
//* `Map.Entry.comparingByValue()` is a static method that creates a comparator based on the entry’s value.
//* Normally it sorts ascending (lowest score → highest).
//* We wrap it with `Comparator.reverseOrder()` to flip it → so now we get **highest scores first**.
//* The `<PersonalityTrait, Integer>` part is a **generic type hint** to tell the compiler exactly what types our `Map.Entry` has.
//
//So this line sorts the stream by score in **descending order**.
//
//---
//
//### 3. `.limit(3)`
//
//* Takes only the **top 3 entries** from the sorted stream.
//* So we don’t process the entire map if we only need the best three.
//
//---
//
//### 4. `.map(Map.Entry::getKey)`
//
//* `map()` transforms each `Map.Entry<PersonalityTrait, Integer>` into just the `PersonalityTrait` (the key).
//* `Map.Entry::getKey` is a **method reference** → shorthand for `(entry) -> entry.getKey()`.
//
//Now the stream contains only `PersonalityTrait` values.
//
//---
//
//### 5. `.toList()`
//
//* Collects the stream into a `List<PersonalityTrait>`.
//* In Java 16+, `.toList()` gives you an unmodifiable list.
//
//  * In earlier versions, you’d use `.collect(Collectors.toList())`.
//
//---
//
//✅ **Final result:**
//A list of the top 3 personality traits (by score), sorted from highest to lowest.
//
//---
//
//👉 Do you want me to also rewrite this with a **for-each loop style** (no streams), so you can see the traditional way vs. the modern stream way?
