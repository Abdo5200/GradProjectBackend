package com.example.gradproject.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface CookieService {

    /**
     * Creates and sets a refresh token cookie in the HTTP response
     * 
     * @param response the HTTP response
     * @param refreshToken the refresh token value
     */
    void setRefreshTokenCookie(HttpServletResponse response, String refreshToken);

    /**
     * Clears the refresh token cookie from the HTTP response
     * 
     * @param response the HTTP response
     */
    void clearRefreshTokenCookie(HttpServletResponse response);

    /**
     * Extracts the refresh token from cookies in the HTTP request
     * 
     * @param request the HTTP request
     * @return the refresh token value, or null if not found
     */
    String extractRefreshTokenFromCookie(HttpServletRequest request);
}

