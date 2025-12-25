package com.capstone.personalityTest.repository;

import com.capstone.personalityTest.model.RefreshToken;
import com.capstone.personalityTest.model.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    void deleteByToken(String token);
}

