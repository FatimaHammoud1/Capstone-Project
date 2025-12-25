package com.capstone.personalityTest.service;

import com.capstone.personalityTest.model.RefreshToken;
import com.capstone.personalityTest.model.UserInfo;
import com.capstone.personalityTest.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class RefreshTokenService {

    @Value("${app.refreshTokenDurationDays:1}")
    private int refreshTokenDurationDays;

    private final RefreshTokenRepository refreshTokenRepo;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepo) {
        this.refreshTokenRepo = refreshTokenRepo;
    }

    // Generate a new refresh token for a user
    public RefreshToken createRefreshToken(UserInfo user) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString()); // random opaque string
        refreshToken.setExpiryDate(Instant.now().plus(refreshTokenDurationDays, ChronoUnit.DAYS));
        return refreshTokenRepo.save(refreshToken);
    }

    // Validate the token and return user
    public UserInfo verifyRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepo.findByToken(token)
                .orElseThrow(() -> new IllegalStateException("Refresh token not found"));

        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepo.delete(refreshToken);
            throw new IllegalStateException("Refresh token expired");
        }

        return refreshToken.getUser();
    }

    // Optional: delete refresh token (logout)
    public void deleteRefreshToken(String token) {
        refreshTokenRepo.deleteByToken(token); // call repository
    }
}
