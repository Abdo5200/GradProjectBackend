package com.example.gradproject.service;

import jakarta.servlet.http.HttpServletRequest;

public interface TokenExtractionService {

    /**
     * Extracts the Bearer token from the Authorization header
     * 
     * @param request the HTTP request
     * @return the token value, or null if not found or invalid format
     */
    String extractBearerToken(HttpServletRequest request);
}

