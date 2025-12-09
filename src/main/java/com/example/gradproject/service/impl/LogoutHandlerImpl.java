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
import com.example.gradproject.service.TokenManagementService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Service
public class LogoutHandlerImpl implements LogoutHandler {

    private static final Logger logger = LoggerFactory.getLogger(LogoutHandlerImpl.class);

    private final AuthService authService;
    private final CookieService cookieService;
    private final TokenManagementService tokenManagementService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;

    public LogoutHandlerImpl(AuthService authService, CookieService cookieService,
            TokenManagementService tokenManagementService,
            RefreshTokenRepository refreshTokenRepository,
            JwtUtil jwtUtil) {
        this.authService = authService;
        this.cookieService = cookieService;
        this.tokenManagementService = tokenManagementService;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Map<String, String> handleLogout(HttpServletRequest request, HttpServletResponse response) {
        try {
            String token = tokenManagementService.extractBearerToken(request);
            String refreshToken = cookieService.extractRefreshTokenFromCookie(request);

            // Primary: Use refresh token (HttpOnly cookie) for user identification
            // This works even when access token is expired
            if (refreshToken != null) {
                try {
                    String username = jwtUtil.extractUsername(refreshToken);
                    String deviceId = jwtUtil.extractDeviceId(refreshToken);

                    if (username != null && deviceId != null && !deviceId.isEmpty()) {
                        String compositeId = username + ":" + deviceId;
                        refreshTokenRepository.deleteById(compositeId);
                        logger.info("Deleted refresh token for user: {} on device: {} (ID: {})",
                                username, deviceId, compositeId);
                    }
                } catch (Exception e) {
                    logger.warn("Could not extract user info from refresh token during logout: {}", e.getMessage());
                }
            }

            // Best-effort: Blacklist access token if it's valid (not expired)
            if (token != null) {
                try {
                    // Only blacklist if token is not expired (otherwise it's already invalid)
                    if (!jwtUtil.isTokenExpired(token)) {
                        authService.logout(token);
                        logger.info("Blacklisted access token during logout");
                    }
                } catch (Exception e) {
                    // Access token might be expired or malformed - that's OK for logout
                    logger.debug("Could not blacklist access token (might be expired): {}", e.getMessage());
                }
            }

            // Always clear the refresh token cookie
            cookieService.clearRefreshTokenCookie(response);

            return Map.of("message", "Logout successful");

        } catch (Exception e) {
            logger.error("Error during logout", e);
            return Map.of("error", "Error during logout: " + e.getMessage());
        }
    }
}
