package com.example.gradproject.service;

import jakarta.servlet.http.HttpServletRequest;

public interface TokenManagementService {

    String extractBearerToken(HttpServletRequest request);

    void cleanupExpiredTokens();
}
