package com.capstone.personalityTest.service;

import com.capstone.personalityTest.model.UserInfo;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;


import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/*
 * This class adapts your UserInfo entity to Spring Security's UserDetails interface.
 * Spring Security uses UserDetails to get user info, password, and authorities (roles) for authentication and authorization.
 */

public class UserInfoDetails implements UserDetails {
    @Getter
    private final Long id;
    private final String username; // Stores the user's email as the username
    private final String password; // Stores the user's hashed password
    private final List<GrantedAuthority> authorities; // Stores roles/authorities in a format Spring Security understands

    // Constructor takes a UserInfo entity and maps its fields to UserDetails
    public UserInfoDetails(UserInfo userInfo) {
        this.id = userInfo.getId();
        this.username = userInfo.getEmail(); // Use email as username for login
        this.password = userInfo.getPassword(); // Store hashed password

        this.authorities = userInfo.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getCode()))
                .collect(Collectors.toList());
    }



    // Return the authorities (roles) of the user
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }
    // Return the username (email) of the user
    @Override
    public String getUsername() {
        return username;
    }
    // Return the hashed password of the user
    @Override
    public String getPassword() {
        return password;
    }

    // Account is considered non-expired (true means not expired)
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    // Account is considered non-locked
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    // Credentials (password) are considered non-expired
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    // Account is considered enabled
    @Override
    public boolean isEnabled() {
        return true;
    }
}
