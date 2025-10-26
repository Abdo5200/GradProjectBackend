package com.example.gradproject.service.impl;

import com.example.gradproject.DTO.ForgotPasswordRequest;
import com.example.gradproject.DTO.ForgotPasswordResponse;
import com.example.gradproject.DTO.ResetPasswordRequest;
import com.example.gradproject.DTO.ResetPasswordResponse;
import com.example.gradproject.Repository.UserRepo;
import com.example.gradproject.entity.User;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class PasswordResetService {

    private final UserRepo userRepo;

    private final EmailService emailService;

    private final PasswordEncoder passwordEncoder;

    @Autowired
    public PasswordResetService(UserRepo userRepo, EmailService emailService, PasswordEncoder passwordEncoder) {
        this.emailService = emailService;
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    public ForgotPasswordResponse forgotPassword(ForgotPasswordRequest request) {
        try {
            Optional<User> optionalUser = userRepo.findByEmail(request.getEmail());

            if (optionalUser.isEmpty()) {
                return new ForgotPasswordResponse(
                        "This Email is not registered",
                        false
                );
            }

            User user = optionalUser.get();

            String resetToken = UUID.randomUUID().toString();
            user.setResetToken(resetToken);
            user.setResetTokenExpiry(LocalDateTime.now().plusHours(1)); // 1 hour expiry

            userRepo.save(user);

            emailService.sendPasswordResetEmail(
                    user.getEmail(),
                    user.getFirstName(),
                    resetToken
            );

            return new ForgotPasswordResponse(
                    "If your email is registered, you will receive a password reset link.",
                    true
            );

        } catch (Exception e) {
            return new ForgotPasswordResponse(
                    "An error occurred while processing your request.",
                    false
            );
        }
    }

    public ResetPasswordResponse resetPassword(ResetPasswordRequest request) {
        try {
            Optional<User> optionalUser = userRepo.findByResetToken(request.getToken());

            if (optionalUser.isEmpty()) {
                return new ResetPasswordResponse("Invalid reset token.", false);
            }

            User user = optionalUser.get();

            if (user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
                return new ResetPasswordResponse("Reset token has expired.", false);
            }

            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            user.setResetToken(null);
            user.setResetTokenExpiry(null);

            userRepo.save(user);

            return new ResetPasswordResponse("Password reset successfully.", true);

        } catch (Exception e) {
            return new ResetPasswordResponse(
                    "An error occurred while resetting your password.",
                    false
            );
        }
    }
}
