package com.capstone.personalityTest.model;

import com.capstone.personalityTest.PersonalityTestApplication;
import com.capstone.personalityTest.model.Enum.PersonalityTrait;
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
public class PersonalityResult {

    @Enumerated(EnumType.STRING)
    private PersonalityTrait firstTrait;// Highest score, e.g., "Investigative"
    @Enumerated(EnumType.STRING)
    private PersonalityTrait secondTrait;// Second highest, e.g., "Social
    @Enumerated(EnumType.STRING)
    private PersonalityTrait thirdTrait;   // Third highest, e.g., "Artistic"


    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "personality_result_scores",
            joinColumns = @JoinColumn(name = "test_attempt_id")
    )
    @MapKeyColumn(name = "trait")   // <-- KEY
    @Column(name = "score")         // <-- VALUE
    @Enumerated(EnumType.STRING)
    //@MapKeyEnumerated(EnumType.STRING)
    private Map<PersonalityTrait, Integer> traitScores;

    // e.g., { "R"=5, "I"=7, "C"=3, "S"=4, "E"=6, "A"=2 }

    // Helper to compute top 3 traits automatically
    public void calculateTopTraits() {
        List<PersonalityTrait> topTraits = traitScores.entrySet().stream()
                .sorted(Map.Entry.<PersonalityTrait, Integer>comparingByValue(Comparator.reverseOrder()))
                .limit(3)
                .map(Map.Entry::getKey) // extract the PersonalityTrait keys
                .toList();

        firstTrait = !topTraits.isEmpty() ? topTraits.get(0) : null;
        secondTrait = topTraits.size() > 1 ? topTraits.get(1) : null;
        thirdTrait = topTraits.size() > 2 ? topTraits.get(2) : null;
    }

    @Override
    public String toString() {
        return String.join("-",
                firstTrait != null ? firstTrait.name() : "",
                secondTrait != null ? secondTrait.name() : "",
                thirdTrait != null ? thirdTrait.name() : "");
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
