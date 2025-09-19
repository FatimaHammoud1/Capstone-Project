package com.capstone.personalityTest.service;

import com.capstone.personalityTest.dto.RequestDTO.UserUpdateRequest;
import com.capstone.personalityTest.dto.ResponseDTO.UserInfoResponse;
import com.capstone.personalityTest.dto.RequestDTO.UserInfoRequest;
import com.capstone.personalityTest.exception.FoundException;
import com.capstone.personalityTest.exception.NotFoundException;
import com.capstone.personalityTest.mapper.UserMapper;
import com.capstone.personalityTest.model.Enum.Role;
import com.capstone.personalityTest.model.UserInfo;
import com.capstone.personalityTest.repository.UserInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    @Autowired
    public UserInfoService(UserInfoRepository userRepo, PasswordEncoder encoder, UserMapper userMapper) {
        this.userRepo = userRepo;
        this.encoder = encoder;
        this.userMapper = userMapper;
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
    public String addUser(UserInfoRequest userInfoRequest) {
        //No repetition for user
        if (userRepo.findByEmail(userInfoRequest.getEmail()).isPresent()) {
            throw new FoundException("User with email " + userInfoRequest.getEmail() + " already exists");
        }

        // Encrypt password before saving
        userInfoRequest.setPassword(encoder.encode(userInfoRequest.getPassword()));
        UserInfo userInfo = userMapper.toEntity(userInfoRequest);
        if (userInfo.getRoles() == null || userInfo.getRoles().isEmpty()) {
            userInfo.setRoles(Set.of(Role.ROLE_USER));
        }

        userRepo.save(userInfo);
        return "User added successfully!";
    }

    public Page<UserInfoResponse> getAllUsers(Pageable pageable) {
        Page<UserInfo> userPages = userRepo.findAll(pageable);
        return userPages.map(user -> userMapper.toResponse(user));

    }

    public UserInfoResponse getUserById(int id) {
        UserInfo userById = userRepo.findById(id).orElseThrow(()->new NotFoundException("User with id " + id + " not found"));
        return userMapper.toResponse(userById);
    }

    public boolean deleteUser(int id, Pageable pageable) {
        Optional<UserInfo> optionalUser = userRepo.findById(id);
            userRepo.deleteById(id);
            return true;
    }

    public boolean updateUser(int id, UserUpdateRequest userUpdateRequest) {
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
