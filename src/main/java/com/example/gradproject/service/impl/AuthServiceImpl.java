package com.example.gradproject.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.gradproject.config.JwtUtil;
import com.example.gradproject.service.AuthService;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final TokenBlacklistService tokenBlacklistService;
    private final JwtUtil jwtUtil;

    public AuthServiceImpl(TokenBlacklistService tokenBlacklistService, JwtUtil jwtUtil) {
        this.tokenBlacklistService = tokenBlacklistService;
        this.jwtUtil = jwtUtil;
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
}
