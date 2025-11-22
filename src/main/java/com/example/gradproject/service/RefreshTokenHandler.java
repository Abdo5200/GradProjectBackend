package com.example.gradproject.service;

import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

public interface RefreshTokenHandler {

    /**
     * Handles refresh token request, extracting token from cookies and generating new access token
     * 
     * @param request the HTTP request containing the refresh token cookie
     * @return a map containing the new access token or error message
     */
    Map<String, String> handleRefreshToken(HttpServletRequest request);
}

