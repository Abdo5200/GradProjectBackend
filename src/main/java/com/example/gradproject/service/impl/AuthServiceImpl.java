package com.example.gradproject.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.gradproject.Repository.RefreshTokenRepository;
import com.example.gradproject.config.JwtUtil;
import com.example.gradproject.entity.RefreshToken;
import com.example.gradproject.service.AuthService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Optional;

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
    public Map<String, String> refreshToken(String refreshToken) {
        Map<String, String> response = new HashMap<>();
        try {
            // Validate token structure and expiration
            if (!jwtUtil.validateToken(refreshToken)) {
                response.put("error", "Invalid refresh token");
                return response;
            }

            // Check if token exists in Redis
            Optional<RefreshToken> storedToken = refreshTokenRepository.findByToken(refreshToken);
            if (storedToken.isEmpty()) {
                response.put("error", "Refresh token not found or expired");
                return response;
            }

            // Additional check: Verify stored token hasn't expired
            RefreshToken tokenEntity = storedToken.get();
            if (tokenEntity.getExpiryDate() != null && 
                tokenEntity.getExpiryDate().before(new java.util.Date())) {
                // Token expired, remove it from Redis
                refreshTokenRepository.deleteByToken(refreshToken);
                response.put("error", "Refresh token has expired");
                return response;
            }

            // Get user details
            String username = jwtUtil.extractUsername(refreshToken);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // Generate new access token
            String newAccessToken = jwtUtil.generateToken(userDetails);

            response.put("accessToken", newAccessToken);
            return response;

        } catch (Exception e) {
            logger.error("Error refreshing token", e);
            response.put("error", "Error refreshing token");
            return response;
        }
    }
}
