package com.example.gradproject.service.impl;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.gradproject.config.JwtUtil;
import com.example.gradproject.service.AuthService;
import com.example.gradproject.service.CookieService;
import com.example.gradproject.service.RefreshTokenHandler;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class RefreshTokenHandlerImpl implements RefreshTokenHandler {

    private final AuthService authService;
    private final CookieService cookieService;
    private final JwtUtil jwtUtil;

    public RefreshTokenHandlerImpl(AuthService authService, CookieService cookieService, JwtUtil jwtUtil) {
        this.authService = authService;
        this.cookieService = cookieService;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Map<String, String> handleRefreshToken(HttpServletRequest request) {
        String refreshToken = cookieService.extractRefreshTokenFromCookie(request);

        if (refreshToken == null) {
            return Map.of("error", "Refresh token is missing");
        }

        // Extract deviceId from JWT refresh token
        String deviceId;
        try {
            deviceId = jwtUtil.extractDeviceId(refreshToken);
            if (deviceId == null || deviceId.isEmpty()) {
                return Map.of("error", "Device ID not found in refresh token");
            }
        } catch (Exception e) {
            return Map.of("error", "Invalid refresh token format");
        }

        return authService.refreshToken(refreshToken, deviceId);
    }
}
