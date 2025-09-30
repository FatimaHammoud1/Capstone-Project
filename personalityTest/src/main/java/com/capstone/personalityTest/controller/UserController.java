package com.capstone.personalityTest.controller;

import com.capstone.personalityTest.dto.RequestDTO.AuthRequest;
import com.capstone.personalityTest.dto.RequestDTO.UserInfoRequest;
import com.capstone.personalityTest.dto.RequestDTO.UserUpdateRequest;
import com.capstone.personalityTest.dto.ResponseDTO.JwtResponse;
import com.capstone.personalityTest.dto.ResponseDTO.UserInfoResponse;
import com.capstone.personalityTest.service.JwtService;
import com.capstone.personalityTest.service.UserInfoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class UserController {

    // Service for user operations (adding/fetching users)
    private final UserInfoService service;

    // Service for handling JWT token creation/validation
    private final JwtService jwtService;

    // Manages authentication by checking credentials against UserDetailsService
    private final AuthenticationManager authenticationManager;

    @GetMapping("/welcome")
    public String welcome() {
        return "Welcome this endpoint is not secure";
    }

    @PostMapping("/signUp")
    public ResponseEntity<UserInfoResponse> addNewUser(@Valid @RequestBody UserInfoRequest userInfoRequest) {
        UserInfoResponse userInfoResponse = service.addUser(userInfoRequest);
        return new ResponseEntity<>(userInfoResponse , HttpStatus.CREATED);
    }

    // Removed the role checks here as they are already managed in SecurityConfig

    //logIn endpoint
    @PostMapping("/signIn")
    public JwtResponse authenticateAndGetToken(@Valid @RequestBody AuthRequest authRequest) {
        // Authenticate the user by creating a UsernamePasswordAuthenticationToken
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
        );
        // If authentication is successful, generate a JWT token for the user ,else throw an exception
        if (authentication.isAuthenticated()) {
            String token =  jwtService.generateToken(authRequest.getUsername());
            return new JwtResponse(token);
        } else {
            throw new UsernameNotFoundException("Invalid user request!");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        return ResponseEntity.ok("Logged out successfully");
    }


    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users")
    public ResponseEntity<Page<UserInfoResponse>> getAllUsers (Pageable pageable){
        Page<UserInfoResponse> usersPage = service.getAllUsers(pageable);
        return new ResponseEntity<>(usersPage, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping("/users/{id}")
    public ResponseEntity<UserInfoResponse> getUserById (@PathVariable Long id){
        UserInfoResponse userById = service.getUserById(id);
        return new ResponseEntity<>(userById , HttpStatus.OK);
    }
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/users/{id}")
    public ResponseEntity<String> deleteUser (@PathVariable Long id, Pageable pageable){
        if(service.deleteUser(id , pageable))
            return new ResponseEntity<>("User deleted successfully" , HttpStatus.OK);
        return new ResponseEntity<>(null , HttpStatus.BAD_REQUEST);
    }
    @PreAuthorize("hasRole('USER')")
    @PutMapping("/users/{id}")
    public ResponseEntity<String> updateUser (@PathVariable Long id ,
                                              @RequestBody UserUpdateRequest userUpdateRequest){
        if(service.updateUser(id , userUpdateRequest))
            return new ResponseEntity<>("User updated Successfully" , HttpStatus.OK);
        return new ResponseEntity<>(null , HttpStatus.BAD_REQUEST);

    }

}
