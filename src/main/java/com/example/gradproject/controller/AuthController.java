package com.example.gradproject.controller;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.gradproject.DTO.ForgotPasswordRequest;
import com.example.gradproject.DTO.ForgotPasswordResponse;
import com.example.gradproject.DTO.LoginRequest;
import com.example.gradproject.DTO.LoginResponse;
import com.example.gradproject.DTO.ResetPasswordRequest;
import com.example.gradproject.DTO.ResetPasswordResponse;
import com.example.gradproject.DTO.SignupRequest;
import com.example.gradproject.DTO.SignupResponse;
import com.example.gradproject.service.AuthService;
import com.example.gradproject.service.UserService;
import com.example.gradproject.service.impl.PasswordResetService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController()
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final UserService userService;
    private final PasswordResetService passwordResetService;
    private final AuthService authService;

    public AuthController(UserService userService, PasswordResetService passwordResetService,
            AuthService authService) {
        this.userService = userService;
        this.passwordResetService = passwordResetService;
        this.authService = authService;
    }

    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> postSignUp(@Valid @RequestBody SignupRequest signupRequest) {
        logger.info("Recieved Signup Request");
        SignupResponse signupResponse = userService.registerUser(signupRequest);
        if (signupResponse.isSuccess())
            return ResponseEntity.ok(signupResponse);
        else
            return ResponseEntity.badRequest().body(signupResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> postLogin(@Valid @RequestBody LoginRequest loginRequest,
            HttpServletResponse response) {
        LoginResponse loginResponse = userService.authenticateUser(loginRequest);
        if (loginResponse.isSuccess()) {
            // Set refresh token as HttpOnly cookie
            Cookie cookie = new Cookie("refreshToken", loginResponse.getRefreshToken());
            cookie.setHttpOnly(true);
            cookie.setSecure(false); // Set to true in production (HTTPS)
            cookie.setPath("/");
            cookie.setMaxAge(30 * 24 * 60 * 60); // 30 days
            response.addCookie(cookie);

            // Remove refresh token from response body
            loginResponse.setRefreshToken(null);

            return ResponseEntity.ok(loginResponse);
        } else {
            return ResponseEntity.badRequest().body(loginResponse);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest request, HttpServletResponse response) {
        try {
            // Clear refresh token cookie
            Cookie cookie = new Cookie("refreshToken", null);
            cookie.setHttpOnly(true);
            cookie.setSecure(false);
            cookie.setPath("/");
            cookie.setMaxAge(0);
            response.addCookie(cookie);

            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                Map<String, String> logoutResponse = authService.logout(token);
                return ResponseEntity.ok(logoutResponse);
            }

            Map<String, String> responseMap = new HashMap<>();
            responseMap.put("message", "Logout successful");
            return ResponseEntity.ok(responseMap);
        } catch (Exception e) {
            logger.error("Error during logout", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error during logout: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<Map<String, String>> refreshToken(HttpServletRequest request) {
        String refreshToken = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refreshToken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }

        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Refresh token is missing"));
        }

        Map<String, String> response = authService.refreshToken(refreshToken);
        if (response.containsKey("error")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ForgotPasswordResponse> postForgotPassword(
            @Valid @RequestBody ForgotPasswordRequest forgotPasswordRequest) {
        ForgotPasswordResponse forgotPasswordResponse = passwordResetService.forgotPassword(forgotPasswordRequest);
        if (!forgotPasswordResponse.isSuccess())
            return ResponseEntity.badRequest().body(forgotPasswordResponse);
        return ResponseEntity.ok(forgotPasswordResponse);
    }

    @PatchMapping("/reset-password")
    public ResponseEntity<ResetPasswordResponse> postResetPassword(
            @Valid @RequestBody ResetPasswordRequest resetPasswordRequest) {
        ResetPasswordResponse resetPasswordResponse = passwordResetService.resetPassword(resetPasswordRequest);
        if (!resetPasswordResponse.isSuccess())
            return ResponseEntity.badRequest().body(resetPasswordResponse);
        return ResponseEntity.ok(resetPasswordResponse);
    }

}
