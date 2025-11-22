package com.example.gradproject.service;

import com.example.gradproject.DTO.LoginRequest;
import com.example.gradproject.DTO.LoginResponse;

import jakarta.servlet.http.HttpServletResponse;

public interface LoginResponseHandler {

    /**
     * Handles user login, including setting the refresh token cookie
     * 
     * @param loginRequest the login request
     * @param response the HTTP response for setting cookies
     * @return the login response with refresh token removed from body
     */
    LoginResponse handleLogin(LoginRequest loginRequest, HttpServletResponse response);
}

