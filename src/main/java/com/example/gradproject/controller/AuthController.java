package com.example.gradproject.controller;

import com.example.gradproject.DTO.*;
import com.example.gradproject.config.JwtUtil;
import com.example.gradproject.service.UserService;
import com.example.gradproject.service.impl.PasswordResetService;
import com.example.gradproject.service.impl.TokenBlacklistService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController()
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    private final PasswordResetService passwordResetService;
    private final TokenBlacklistService tokenBlacklistService;
    private final JwtUtil jwtUtil;

    @Autowired
    public AuthController(UserService userService, PasswordResetService passwordResetService,
                          TokenBlacklistService tokenBlacklistService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.passwordResetService = passwordResetService;
        this.tokenBlacklistService = tokenBlacklistService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> postSignUp(@Valid @RequestBody SignupRequest signupRequest) {
        SignupResponse signupResponse = userService.registerUser(signupRequest);
        if (signupResponse.isSuccess())
            return ResponseEntity.ok(signupResponse);
        else
            return ResponseEntity.badRequest().body(signupResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> postLogin(@Valid @RequestBody LoginRequest loginRequest) {
        LoginResponse loginResponse = userService.authenticateUser(loginRequest);
        if (loginResponse.isSuccess())
            return ResponseEntity.ok(loginResponse);
        else
            return ResponseEntity.badRequest().body(loginResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            // Blacklist the token
            long expirationTime = jwtUtil.extractExpiration(token).getTime();
            tokenBlacklistService.blacklistToken(token, expirationTime);
        }

        Map<String, String> response = new HashMap<>();
        response.put("message", "Logout successful");
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
