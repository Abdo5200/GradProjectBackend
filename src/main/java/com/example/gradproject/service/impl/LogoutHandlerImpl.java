package com.example.gradproject.service.impl;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.gradproject.service.AuthService;
import com.example.gradproject.service.CookieService;
import com.example.gradproject.service.LogoutHandler;
import com.example.gradproject.service.TokenExtractionService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Service
public class LogoutHandlerImpl implements LogoutHandler {

    private static final Logger logger = LoggerFactory.getLogger(LogoutHandlerImpl.class);

    private final AuthService authService;
    private final CookieService cookieService;
    private final TokenExtractionService tokenExtractionService;

    public LogoutHandlerImpl(AuthService authService, CookieService cookieService,
            TokenExtractionService tokenExtractionService) {
        this.authService = authService;
        this.cookieService = cookieService;
        this.tokenExtractionService = tokenExtractionService;
    }

    @Override
    public Map<String, String> handleLogout(HttpServletRequest request, HttpServletResponse response) {
        try {
            // Clear refresh token cookie
            cookieService.clearRefreshTokenCookie(response);

            // Extract and blacklist the access token if present
            String token = tokenExtractionService.extractBearerToken(request);
            if (token != null) {
                return authService.logout(token);
            }

            // Return success even if no token was provided
            return Map.of("message", "Logout successful");

        } catch (Exception e) {
            logger.error("Error during logout", e);
            return Map.of("error", "Error during logout: " + e.getMessage());
        }
    }
}

