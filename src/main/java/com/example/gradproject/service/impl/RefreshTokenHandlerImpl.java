package com.example.gradproject.service.impl;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.gradproject.service.AuthService;
import com.example.gradproject.service.CookieService;
import com.example.gradproject.service.RefreshTokenHandler;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class RefreshTokenHandlerImpl implements RefreshTokenHandler {

    private final AuthService authService;
    private final CookieService cookieService;

    public RefreshTokenHandlerImpl(AuthService authService, CookieService cookieService) {
        this.authService = authService;
        this.cookieService = cookieService;
    }

    @Override
    public Map<String, String> handleRefreshToken(HttpServletRequest request) {
        String refreshToken = cookieService.extractRefreshTokenFromCookie(request);

        if (refreshToken == null) {
            return Map.of("error", "Refresh token is missing");
        }

        return authService.refreshToken(refreshToken);
    }
}

