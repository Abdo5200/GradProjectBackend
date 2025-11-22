package com.example.gradproject.service.impl;

import org.springframework.stereotype.Service;

import com.example.gradproject.DTO.LoginRequest;
import com.example.gradproject.DTO.LoginResponse;
import com.example.gradproject.service.CookieService;
import com.example.gradproject.service.LoginResponseHandler;
import com.example.gradproject.service.UserService;

import jakarta.servlet.http.HttpServletResponse;

@Service
public class LoginResponseHandlerImpl implements LoginResponseHandler {

    private final UserService userService;
    private final CookieService cookieService;

    public LoginResponseHandlerImpl(UserService userService, CookieService cookieService) {
        this.userService = userService;
        this.cookieService = cookieService;
    }

    @Override
    public LoginResponse handleLogin(LoginRequest loginRequest, HttpServletResponse response) {
        LoginResponse loginResponse = userService.authenticateUser(loginRequest);
        
        if (loginResponse.isSuccess() && loginResponse.getRefreshToken() != null) {
            // Set refresh token as HttpOnly cookie
            cookieService.setRefreshTokenCookie(response, loginResponse.getRefreshToken());
            
            // Remove refresh token from response body for security
            loginResponse.setRefreshToken(null);
        }
        
        return loginResponse;
    }
}

