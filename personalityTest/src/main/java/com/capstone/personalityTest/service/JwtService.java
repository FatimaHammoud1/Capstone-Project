package com.capstone.personalityTest.service;

import com.capstone.personalityTest.model.UserInfo;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.*;
import java.util.function.Function;

@Component
public class JwtService {

    // Secret key used to sign and verify JWTs. Should be kept safe.
    public static final String SECRET = "5367566859703373367639792F423F452848284D6251655468576D5A71347437";

    @Value("${app.jwtExpirationMs:3600000}")
    private long jwtExpirationMs;

    // Secret key used to sign and verify JWTs. Should be kept safe.
    public String generateToken(UserInfo user) {
        Map<String, Object> claims = new HashMap<>();
        List<String> roles = user.getRoles().stream()
                .map(role -> role.getCode())
                .toList();
        claims.put("roles", roles);  // 👈 add roles to JWT
        claims.put("userId", user.getId());
        return createToken(claims, user.getEmail());
    }
//    public String generateToken(String email) { // Use email as username
//        Map<String, Object> claims = new HashMap<>(); // You can add extra data here if needed
//        return createToken(claims, email); // Call helper to build the token
//    }

    // Build the actual JWT token
    private String createToken(Map<String, Object> claims, String email) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(email) // Store username/email in token
                .setIssuedAt(new Date())  // Token creation time
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs)) // Token expires based on config
                .signWith(getSignKey(), SignatureAlgorithm.HS256) // Sign token with secret key
                .compact(); // Build token string
    }

    // Convert the secret string into a cryptographic key
    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET); // Decode from Base64
        return Keys.hmacShaKeyFor(keyBytes); // Create HMAC-SHA key
    }

    // Extract username (email) from JWT
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public List<String> extractRoles(String token) {
        Claims claims = extractAllClaims(token);
        Object rolesObject = claims.get("roles");
        if (rolesObject instanceof List<?>) {
            return ((List<?>) rolesObject).stream()
                    .map(Object::toString)
                    .toList();
        }
        return Collections.emptyList();
    }

    public Long extractUserId(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("userId", Long.class);
    }


    // Extract expiration date from JWT
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Generic method to extract any claim from the token
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);  // Get all claims first
        return claimsResolver.apply(claims);  // Apply function to extract specific claim
    }

    // Parse the token and extract all claims (data inside token)
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignKey())   // Use the signing key to validate token
                .build()
                .parseClaimsJws(token) // Parse and verify token
                .getBody();  // Return the claims
    }

    // Check if the token is expired
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date()); // True if token expiration is before now
    }

    // Validate token by checking username and expiration
    public Boolean validateToken(String token) {
        return (!isTokenExpired(token)); // Match with the actual user  && Ensure token not expired
    }



}
