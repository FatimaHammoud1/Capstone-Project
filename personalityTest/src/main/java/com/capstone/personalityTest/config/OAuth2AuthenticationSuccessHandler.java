package com.capstone.personalityTest.config;

import com.capstone.personalityTest.model.Enum.Role;
import com.capstone.personalityTest.model.Enum.TargetGender;
import com.capstone.personalityTest.model.UserInfo;
import com.capstone.personalityTest.repository.UserInfoRepository;
import com.capstone.personalityTest.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;

@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final UserInfoRepository userInfoRepository;

    public OAuth2AuthenticationSuccessHandler(JwtService jwtService, UserInfoRepository userInfoRepository) {
        this.jwtService = jwtService;
        this.userInfoRepository = userInfoRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();

        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");

        Optional<UserInfo> userOptional = userInfoRepository.findByEmail(email);
        UserInfo user;

        boolean isNewUser = false;

        if (userOptional.isPresent()) {
            user = userOptional.get();
            // Check if critical info is missing (e.g. they registered but never set gender)
            if (user.getGender() == null || user.getGender() == TargetGender.ALL) {
                isNewUser = true; // Treat as new/incomplete to force update
            }
        } else {
            isNewUser = true;
            user = new UserInfo();
            user.setName(name);
            user.setEmail(email);
            user.setPassword("[GOOGLE_AUTH]");
            user.setGender(TargetGender.ALL); // Placeholder: "Unspecified"
            user.setRoles(Set.of(Role.ROLE_USER));
            
            user = userInfoRepository.save(user);
        }

        String token = jwtService.generateToken(user);
        
        // Pass isNewUser flag to frontend
        String targetUrl = "http://localhost:5173/oauth2/callback?token=" + token + "&isNewUser=" + isNewUser;
        
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}