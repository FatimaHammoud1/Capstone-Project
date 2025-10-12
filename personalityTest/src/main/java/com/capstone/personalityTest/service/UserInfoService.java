package com.capstone.personalityTest.service;

import com.capstone.personalityTest.dto.RequestDTO.AuthRequest;
import com.capstone.personalityTest.dto.RequestDTO.UserUpdateRequest;
import com.capstone.personalityTest.dto.ResponseDTO.JwtResponse;
import com.capstone.personalityTest.dto.ResponseDTO.UserInfoResponse;
import com.capstone.personalityTest.dto.RequestDTO.UserInfoRequest;
import com.capstone.personalityTest.exception.EntityExistsException;
import com.capstone.personalityTest.mapper.UserMapper;
import com.capstone.personalityTest.model.Enum.Role;
import com.capstone.personalityTest.model.UserInfo;
import com.capstone.personalityTest.repository.UserInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@Service
public class UserInfoService implements UserDetailsService {

    private final UserInfoRepository userRepo;

    private final PasswordEncoder encoder;

    private final UserMapper userMapper;

    private final JwtService jwtService;  // ✅ Inject JwtService
    private final AuthenticationManager authenticationManager;
    @Autowired
    public UserInfoService(UserInfoRepository userRepo, PasswordEncoder encoder, UserMapper userMapper , JwtService jwtService , AuthenticationManager authenticationManager) {
        this.userRepo = userRepo;
        this.encoder = encoder;
        this.userMapper = userMapper;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    // Method to load user details by username (email)
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Fetch user from the database by email (username)
        Optional<UserInfo> userInfo = userRepo.findByEmail(username);

        if (userInfo.isEmpty()) {
            throw new UsernameNotFoundException("User not found with email: " + username);
        }

        // Convert UserInfo to UserDetails (UserInfoDetails)
        UserInfo user = userInfo.get();
//        return new User(user.getEmail(), user.getPassword(), user.getRoles());
        return new UserInfoDetails(user);

    }

    // Add any additional methods for registering or managing users
    public void addUser(UserInfoRequest userInfoRequest) {
        //No repetition for user
        if (userRepo.findByEmail(userInfoRequest.getEmail()).isPresent()) {
            throw new EntityExistsException("User with email " + userInfoRequest.getEmail() + " already exists");
        }

        // Encrypt password before saving
        userInfoRequest.setPassword(encoder.encode(userInfoRequest.getPassword()));
        UserInfo userInfo = userMapper.toEntity(userInfoRequest);
        if (userInfo.getRoles() == null || userInfo.getRoles().isEmpty()) {
            userInfo.setRoles(Set.of(Role.ROLE_USER));
        }

        userRepo.save(userInfo);
    }

    public JwtResponse authenticate(AuthRequest authRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
        );

        if (!authentication.isAuthenticated()) {
            throw new UsernameNotFoundException("Invalid user credentials");
        }

        UserInfo user = userRepo.findByEmail(authRequest.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String token = jwtService.generateToken(user);
        return new JwtResponse(token);
    }

    public Page<UserInfoResponse> getAllUsers(Pageable pageable) {
        Page<UserInfo> userPages = userRepo.findAll(pageable);
        return userPages.map(user -> userMapper.toResponse(user));

    }

    public UserInfoResponse getUserById(Long id) {
        Optional<UserInfo> optionalUserInfo = userRepo.findById(id);
        if(optionalUserInfo.isEmpty())
            throw new UsernameNotFoundException("User with id " + id + " not found");
        UserInfo userById = optionalUserInfo.get();
        return userMapper.toResponse(userById);
    }

    public boolean deleteUser(Long id, Pageable pageable) {
        Optional<UserInfo> optionalUser = userRepo.findById(id);
            userRepo.deleteById(id);
            return true;
    }

    public boolean updateUser(Long id, UserUpdateRequest userUpdateRequest) {
        Optional<UserInfo> optionalUser = userRepo.findById(id);
        if(optionalUser.isPresent()){
            UserInfo user = optionalUser.get();
            userMapper.updateUserFromDTO(userUpdateRequest, user);
            user.setPassword(encoder.encode(userUpdateRequest.getPassword()));
            userRepo.save(user);
            return true;
        }
        return false;

    }




}
