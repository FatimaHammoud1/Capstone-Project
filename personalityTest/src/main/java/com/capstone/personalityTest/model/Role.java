package com.capstone.personalityTest.model;



import com.capstone.personalityTest.model.UserInfo;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = "users")
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code; // DEVELOPER, ORG_OWNER, STUDENT, ...

    private String name;
    private String description;

    @ManyToMany(mappedBy = "roles")
    private Set<UserInfo> users = new HashSet<>();


}
