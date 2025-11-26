package com.example.gradproject.service.impl;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.gradproject.Repository.RefreshTokenRepository;
import com.example.gradproject.config.JwtUtil;
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
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;

    public LogoutHandlerImpl(AuthService authService, CookieService cookieService,
            TokenExtractionService tokenExtractionService,
            RefreshTokenRepository refreshTokenRepository,
            JwtUtil jwtUtil) {
        this.authService = authService;
        this.cookieService = cookieService;
        this.tokenExtractionService = tokenExtractionService;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Map<String, String> handleLogout(HttpServletRequest request, HttpServletResponse response) {
        try {
            // Extract and blacklist the access token if present
            String token = tokenExtractionService.extractBearerToken(request);
            if (token != null) {
                // Delete all refresh tokens for this user from Redis
                String username = jwtUtil.extractUsername(token);

                // Delete each token individually (more reliable with Redis)
                refreshTokenRepository.findByUsername(username).forEach(refreshTokenRepository::delete);

                logger.info("🔒 Deleted all refresh tokens for user: {}", username);

                // Blacklist the access token
                authService.logout(token);
            }

            // Clear refresh token cookie
            cookieService.clearRefreshTokenCookie(response);

            return Map.of("message", "Logout successful");

        } catch (Exception e) {
            logger.error("Error during logout", e);
            return Map.of("error", "Error during logout: " + e.getMessage());
        }
    }
}
