package com.capstone.personalityTest.controller;

import com.capstone.personalityTest.dto.RequestDTO.AuthRequest;
import com.capstone.personalityTest.dto.RequestDTO.UserInfoRequest;
import com.capstone.personalityTest.dto.RequestDTO.UserUpdateRequest;
import com.capstone.personalityTest.dto.ResponseDTO.JwtResponse;
import com.capstone.personalityTest.dto.ResponseDTO.UserInfoResponse;
import com.capstone.personalityTest.model.RefreshToken;
import com.capstone.personalityTest.model.UserInfo;
import com.capstone.personalityTest.service.JwtService;
import com.capstone.personalityTest.service.RefreshTokenService;
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

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class UserController {

    // Service for user operations (adding/fetching users)
    private final UserInfoService service;

    private final RefreshTokenService refreshTokenService;

    // Service for handling JWT token creation/validation
    private final JwtService jwtService;

    // Manages authentication by checking credentials against UserDetailsService
    private final AuthenticationManager authenticationManager;

    @GetMapping("/welcome")
    public String welcome() {
        return "Welcome this endpoint is not secure";
    }

    @PostMapping("/signUp")
    public ResponseEntity<String> addNewUser(@Valid @RequestBody UserInfoRequest userInfoRequest) {
        service.addUser(userInfoRequest);
        return new ResponseEntity<>(null,HttpStatus.CREATED);
    }

    // Removed the role checks here as they are already managed in SecurityConfig

    //logIn endpoint
    @PostMapping("/signIn")
    public ResponseEntity<JwtResponse> signIn(@Valid @RequestBody AuthRequest authRequest) {
        JwtResponse response = service.authenticate(authRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<JwtResponse> refreshAccessToken(@RequestBody Map<String, String> request) {
        String refreshTokenStr = request.get("refreshToken");

        // Validate refresh token via DB
        UserInfo user = refreshTokenService.verifyRefreshToken(refreshTokenStr);

        // Generate new access token
        String newAccessToken = jwtService.generateToken(user);

        // Rotate refresh token (optional but recommended)
        refreshTokenService.deleteRefreshToken(refreshTokenStr); // delete old token
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user);

        return ResponseEntity.ok(new JwtResponse(newAccessToken, newRefreshToken.getToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestBody Map<String, String> request) {
        String refreshTokenStr = request.get("refreshToken");
        refreshTokenService.deleteRefreshToken(refreshTokenStr); // call service, not repo directly
        return ResponseEntity.ok("Logged out successfully");
    }


   // @PreAuthorize("hasAnyRole('ORG_OWNER', 'DEVELOPER')")
    @GetMapping("/users")
    public ResponseEntity<Page<UserInfoResponse>> getAllUsers (Pageable pageable){
        Page<UserInfoResponse> usersPage = service.getAllUsers(pageable);
        return new ResponseEntity<>(usersPage, HttpStatus.OK);
    }

   // @PreAuthorize("hasAnyRole('ORG_OWNER','USER', 'DEVELOPER')")
    @GetMapping("/users/{id}")
    public ResponseEntity<UserInfoResponse> getUserById (@PathVariable Long id){
        UserInfoResponse userById = service.getUserById(id);
        return new ResponseEntity<>(userById , HttpStatus.OK);
    }
    @PreAuthorize("hasAnyRole('ORG_OWNER', 'DEVELOPER')")
    @DeleteMapping("/users/{id}")
    public ResponseEntity<String> deleteUser (@PathVariable Long id, Pageable pageable){
        if(service.deleteUser(id , pageable))
            return new ResponseEntity<>("User deleted successfully" , HttpStatus.OK);
        return new ResponseEntity<>(null , HttpStatus.BAD_REQUEST);
    }
    @PreAuthorize("hasAnyRole('USER', 'DEVELOPER')")
    @PutMapping("/users/{id}")
    public ResponseEntity<String> updateUser (@PathVariable Long id ,
                                              @RequestBody UserUpdateRequest userUpdateRequest){
        if(service.updateUser(id , userUpdateRequest))
            return new ResponseEntity<>("User updated Successfully" , HttpStatus.OK);
        return new ResponseEntity<>(null , HttpStatus.BAD_REQUEST);

    }

}
