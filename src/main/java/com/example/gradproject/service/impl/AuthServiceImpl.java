package com.example.gradproject.service.impl;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import com.example.gradproject.Repository.RefreshTokenRepository;
import com.example.gradproject.config.JwtUtil;
import com.example.gradproject.entity.RefreshToken;
import com.example.gradproject.service.AuthService;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final TokenBlacklistService tokenBlacklistService;
    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserDetailsService userDetailsService;

    public AuthServiceImpl(TokenBlacklistService tokenBlacklistService, JwtUtil jwtUtil,
            RefreshTokenRepository refreshTokenRepository, UserDetailsService userDetailsService) {
        this.tokenBlacklistService = tokenBlacklistService;
        this.jwtUtil = jwtUtil;
        this.refreshTokenRepository = refreshTokenRepository;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public Map<String, String> logout(String token) {
        long expirationTime = jwtUtil.extractExpiration(token).getTime();
        tokenBlacklistService.blacklistToken(token, expirationTime);
        logger.info("Token blacklisted successfully");

        Map<String, String> response = new HashMap<>();
        response.put("message", "Logout successful");
        return response;
    }

    @Override
    public Map<String, String> refreshToken(String refreshToken, String deviceId) {
        Map<String, String> response = new HashMap<>();
        try {
            // Validate token structure and expiration
            if (!jwtUtil.validateToken(refreshToken)) {
                response.put("error", "Invalid refresh token");
                return response;
            }

            // Extract username from JWT to build composite key
            String username = jwtUtil.extractUsername(refreshToken);

            // Build composite key and check if token exists in Redis
            String compositeId = username + ":" + deviceId;
            Optional<RefreshToken> storedToken = refreshTokenRepository.findById(compositeId);

            if (storedToken.isEmpty()) {
                response.put("error", "Refresh token not found or expired");
                return response;
            }

            RefreshToken tokenEntity = storedToken.get();

            // Verify the stored token matches the provided token
            if (!refreshToken.equals(tokenEntity.getToken())) {
                response.put("error", "Token mismatch - security violation");
                logger.warn("Security Alert: Token mismatch for user: {} on device: {}", username, deviceId);
                return response;
            }

            // Additional check: Verify stored token hasn't expired
            if (tokenEntity.getExpiryDate() != null &&
                    tokenEntity.getExpiryDate().before(new java.util.Date())) {
                // Token expired, remove it from Redis
                refreshTokenRepository.deleteById(compositeId);
                response.put("error", "Refresh token has expired");
                return response;
            }

            // Get user details for generating new access token
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // Generate new access token
            String newAccessToken = jwtUtil.generateToken(userDetails);

            // Update access token hash and last used timestamp
            tokenEntity.setAccessTokenHash(generateTokenHash(newAccessToken));
            tokenEntity.setLastUsed(java.time.LocalDateTime.now());
            refreshTokenRepository.save(tokenEntity);

            response.put("accessToken", newAccessToken);
            logger.info("Refreshed access token for user: {} on device: {}", username, deviceId);
            return response;

        } catch (Exception e) {
            logger.error("Error refreshing token", e);
            response.put("error", "Error refreshing token");
            return response;
        }
    }

    /**
     * Generate SHA-256 hash of the access token for pairing with refresh token
     */
    private String generateTokenHash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(token.getBytes(StandardCharsets.UTF_8));

            // Convert byte array to hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            logger.error("SHA-256 algorithm not found", e);
            // Fallback to absolute value of hashCode if SHA-256 fails
            return String.valueOf(Math.abs(token.hashCode()));
        }
    }
}
