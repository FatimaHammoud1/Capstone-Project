package com.capstone.personalityTest.model.Test;

import com.capstone.personalityTest.model.BaseTest;
import com.capstone.personalityTest.model.Enum.TestStatus;
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
public class Test {

    @Id // jakarta for sql , annotation for mongo
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; //version_id

    private String title;

    private String description;

    private String versionName; //version_name

    @Enumerated(EnumType.STRING)
    private TestStatus status = TestStatus.DRAFT;

    private boolean active = false;

    @ManyToOne
    @JoinColumn(name = "base_test_id")
    private BaseTest baseTest; //related to which test

    @OneToMany(mappedBy = "test", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Section> sections = new ArrayList<>();
}
