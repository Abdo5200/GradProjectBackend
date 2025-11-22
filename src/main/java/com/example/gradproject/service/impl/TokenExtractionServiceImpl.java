package com.example.gradproject.service.impl;

import org.springframework.stereotype.Service;

import com.example.gradproject.service.TokenExtractionService;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class TokenExtractionServiceImpl implements TokenExtractionService {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    public String extractBearerToken(HttpServletRequest request) {
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);
        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            return authHeader.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}

