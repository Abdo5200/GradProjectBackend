package com.example.gradproject.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
import com.example.gradproject.DTO.SessionInfo;
import com.example.gradproject.DTO.SignupRequest;
import com.example.gradproject.DTO.SignupResponse;
import com.example.gradproject.config.JwtUtil;
import com.example.gradproject.service.CookieService;
import com.example.gradproject.service.LoginResponseHandler;
import com.example.gradproject.service.LogoutHandler;
import com.example.gradproject.service.RefreshTokenHandler;
import com.example.gradproject.service.SessionManagementService;
import com.example.gradproject.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController()
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    private final LoginResponseHandler loginResponseHandler;
    private final LogoutHandler logoutHandler;
    private final RefreshTokenHandler refreshTokenHandler;
    private final SessionManagementService sessionManagementService;
    private final CookieService cookieService;
    private final JwtUtil jwtUtil;

    public AuthController(UserService userService, LoginResponseHandler loginResponseHandler,
            LogoutHandler logoutHandler, RefreshTokenHandler refreshTokenHandler,
            SessionManagementService sessionManagementService, CookieService cookieService,
            JwtUtil jwtUtil) {
        this.userService = userService;
        this.loginResponseHandler = loginResponseHandler;
        this.logoutHandler = logoutHandler;
        this.refreshTokenHandler = refreshTokenHandler;
        this.sessionManagementService = sessionManagementService;
        this.cookieService = cookieService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> postSignUp(@Valid @RequestBody SignupRequest signupRequest) {
        SignupResponse signupResponse = userService.registerUser(signupRequest);
        if (signupResponse.isSuccess())
            return ResponseEntity.status(201).body(signupResponse);
        else
            return ResponseEntity.badRequest().body(signupResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> postLogin(@Valid @RequestBody LoginRequest loginRequest,
            HttpServletResponse response) {
        LoginResponse loginResponse = loginResponseHandler.handleLogin(loginRequest, response);
        if (loginResponse.isSuccess()) {
            return ResponseEntity.ok(loginResponse);
        } else {
            return ResponseEntity.badRequest().body(loginResponse);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest request, HttpServletResponse response) {
        Map<String, String> logoutResponse = logoutHandler.handleLogout(request, response);
        if (logoutResponse.containsKey("error")) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(logoutResponse);
        }
        return ResponseEntity.ok(logoutResponse);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<Map<String, String>> refreshToken(HttpServletRequest request) {
        Map<String, String> response = refreshTokenHandler.handleRefreshToken(request);
        if (response.containsKey("error")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ForgotPasswordResponse> postForgotPassword(
            @Valid @RequestBody ForgotPasswordRequest forgotPasswordRequest) {
        ForgotPasswordResponse forgotPasswordResponse = userService.forgotPassword(forgotPasswordRequest);
        if (!forgotPasswordResponse.isSuccess())
            return ResponseEntity.badRequest().body(forgotPasswordResponse);
        return ResponseEntity.ok(forgotPasswordResponse);
    }

    @PatchMapping("/reset-password")
    public ResponseEntity<ResetPasswordResponse> postResetPassword(
            @Valid @RequestBody ResetPasswordRequest resetPasswordRequest) {
        ResetPasswordResponse resetPasswordResponse = userService.resetPassword(resetPasswordRequest);
        if (!resetPasswordResponse.isSuccess())
            return ResponseEntity.badRequest().body(resetPasswordResponse);
        return ResponseEntity.ok(resetPasswordResponse);
    }

    // Session Management Endpoints

    @GetMapping("/sessions")
    public ResponseEntity<List<SessionInfo>> getActiveSessions(
            Authentication authentication,
            HttpServletRequest request) {
        String username = authentication.getName();

        // Extract deviceId from refresh token JWT
        String refreshToken = cookieService.extractRefreshTokenFromCookie(request);
        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(null);
        }

        String currentDeviceId;
        try {
            currentDeviceId = jwtUtil.extractDeviceId(refreshToken);
            if (currentDeviceId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        List<SessionInfo> sessions = sessionManagementService.getActiveSessions(username, currentDeviceId);
        return ResponseEntity.ok(sessions);
    }

    @DeleteMapping("/sessions/{deviceId}")
    public ResponseEntity<Map<String, String>> revokeSession(
            @PathVariable String deviceId,
            Authentication authentication,
            HttpServletRequest request) {
        String username = authentication.getName();

        // Extract current deviceId from refresh token JWT
        String refreshToken = cookieService.extractRefreshTokenFromCookie(request);
        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "No refresh token found"));
        }

        String currentDeviceId;
        try {
            currentDeviceId = jwtUtil.extractDeviceId(refreshToken);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid refresh token"));
        }

        // Prevent revoking current session
        if (deviceId.equals(currentDeviceId)) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Cannot revoke current session. Use logout instead."));
        }

        boolean success = sessionManagementService.revokeSession(username, deviceId);
        if (success) {
            return ResponseEntity.ok(Map.of("message", "Session revoked successfully"));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to revoke session"));
        }
    }

    @DeleteMapping("/sessions/others")
    public ResponseEntity<Map<String, Object>> revokeAllOtherSessions(
            Authentication authentication,
            HttpServletRequest request) {
        String username = authentication.getName();

        // Extract deviceId from refresh token JWT
        String refreshToken = cookieService.extractRefreshTokenFromCookie(request);
        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String currentDeviceId;
        try {
            currentDeviceId = jwtUtil.extractDeviceId(refreshToken);
            if (currentDeviceId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        int revokedCount = sessionManagementService.revokeAllOtherSessions(username, currentDeviceId);
        return ResponseEntity.ok(Map.of(
                "message", "All other sessions revoked successfully",
                "revokedCount", revokedCount));
    }

}
