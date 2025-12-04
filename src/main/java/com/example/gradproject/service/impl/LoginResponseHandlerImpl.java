package com.example.gradproject.service.impl;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.gradproject.DTO.LoginRequest;
import com.example.gradproject.DTO.LoginResponse;
import com.example.gradproject.Repository.RefreshTokenRepository;
import com.example.gradproject.config.JwtUtil;
import com.example.gradproject.service.CookieService;
import com.example.gradproject.service.LoginResponseHandler;
import com.example.gradproject.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Service
public class LoginResponseHandlerImpl implements LoginResponseHandler {

    private static final Logger logger = LoggerFactory.getLogger(LoginResponseHandlerImpl.class);

    private final UserService userService;
    private final CookieService cookieService;
    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;

    public LoginResponseHandlerImpl(UserService userService, CookieService cookieService,
            JwtUtil jwtUtil, RefreshTokenRepository refreshTokenRepository) {
        this.userService = userService;
        this.cookieService = cookieService;
        this.jwtUtil = jwtUtil;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Override
    public LoginResponse handleLogin(LoginRequest loginRequest, HttpServletRequest request,
            HttpServletResponse response) {
        String deviceId = null;

        // Try to extract existing refresh token from cookie
        String existingRefreshToken = cookieService.extractRefreshTokenFromCookie(request);

        if (existingRefreshToken != null) {
            try {
                // Extract deviceId from existing refresh token
                String extractedDeviceId = jwtUtil.extractDeviceId(existingRefreshToken);
                String username = loginRequest.getEmail(); // Username is email

                // Check if this refresh token exists in Redis
                String compositeId = username + ":" + extractedDeviceId;
                if (refreshTokenRepository.existsById(compositeId)) {
                    // Reuse existing deviceId - will replace the session
                    deviceId = extractedDeviceId;
                    logger.info("Reusing existing deviceId for user: {} (device: {})", username, deviceId);

                    // Delete old refresh token (will be replaced with new one)
                    refreshTokenRepository.deleteById(compositeId);
                }
            } catch (Exception e) {
                // Invalid or expired token, generate new deviceId
                logger.debug("Could not extract deviceId from existing refresh token: {}", e.getMessage());
            }
        }

        // If no valid existing deviceId, generate new one
        if (deviceId == null) {
            deviceId = UUID.randomUUID().toString();
            logger.info("Generated new deviceId for user: {} (device: {})", loginRequest.getEmail(), deviceId);
        }

        // Authenticate with device ID (will be embedded in refresh token JWT)
        LoginResponse loginResponse = userService.authenticateUser(loginRequest, deviceId);

        if (loginResponse.isSuccess() && loginResponse.getRefreshToken() != null) {
            // Set refresh token as HttpOnly cookie (deviceId embedded in JWT)
            cookieService.setRefreshTokenCookie(response, loginResponse.getRefreshToken());

            // Remove refresh token from response body for security
            loginResponse.setRefreshToken(null);
        }

        return loginResponse;
    }
}
