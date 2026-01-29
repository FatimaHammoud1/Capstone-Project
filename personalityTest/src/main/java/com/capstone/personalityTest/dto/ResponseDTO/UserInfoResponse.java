package com.capstone.personalityTest.dto.ResponseDTO;


import com.capstone.personalityTest.model.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoResponse {
    private Long id;
    private String name;
    private String email;
    private Set<Role> roles;
}
