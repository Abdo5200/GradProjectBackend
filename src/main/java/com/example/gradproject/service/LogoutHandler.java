package com.example.gradproject.service;

import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface LogoutHandler {

    /**
     * Handles user logout, including token blacklisting and cookie clearing
     * 
     * @param request the HTTP request containing the authorization token
     * @param response the HTTP response for clearing cookies
     * @return a map containing the logout response message
     */
    Map<String, String> handleLogout(HttpServletRequest request, HttpServletResponse response);
}

