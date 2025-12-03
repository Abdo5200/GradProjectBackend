package com.example.gradproject.service.impl;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.gradproject.DTO.ForgotPasswordRequest;
import com.example.gradproject.DTO.ForgotPasswordResponse;
import com.example.gradproject.DTO.LoginRequest;
import com.example.gradproject.DTO.LoginResponse;
import com.example.gradproject.DTO.ResetPasswordRequest;
import com.example.gradproject.DTO.ResetPasswordResponse;
import com.example.gradproject.DTO.SignupRequest;
import com.example.gradproject.DTO.SignupResponse;
import com.example.gradproject.Repository.RefreshTokenRepository;
import com.example.gradproject.Repository.UserRepo;
import com.example.gradproject.config.JwtUtil;
import com.example.gradproject.entity.RefreshToken;
import com.example.gradproject.entity.User;
import com.example.gradproject.exception.UserNotFoundException;
import com.example.gradproject.mappers.SignupRequestUserMapper;
import com.example.gradproject.mappers.UserLoginResponseUserInfoMapper;
import com.example.gradproject.service.UserService;

import jakarta.transaction.Transactional;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepo userRepo;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    private final SignupRequestUserMapper signupRequestUserMapper;
    private final UserLoginResponseUserInfoMapper userLoginResponseUserInfoMapper;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    public UserServiceImpl(UserRepo userRepo,
            AuthenticationManager authenticationManager, JwtUtil jwtUtil,
            RefreshTokenRepository refreshTokenRepository,
            SignupRequestUserMapper signupRequestUserMapper,
            UserLoginResponseUserInfoMapper userLoginResponseUserInfoMapper,
            EmailService emailService,
            PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.refreshTokenRepository = refreshTokenRepository;
        this.signupRequestUserMapper = signupRequestUserMapper;
        this.userLoginResponseUserInfoMapper = userLoginResponseUserInfoMapper;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public User findById(Integer id) {
        Optional<User> user = userRepo.findById(id);
        if (user.isEmpty())
            throw new UserNotFoundException("User with id " + id + " does not exist");
        return user.get();
    }

    @Override
    @Transactional
    public SignupResponse registerUser(SignupRequest signupRequest) {
        try {

            Optional<User> existedUser = userRepo.findByEmail(signupRequest.getEmail());
            if (existedUser.isPresent()) {
                return new SignupResponse("Email is already registered", false, null);
            }

            if (!signupRequest.getPassword().equals(signupRequest.getConfirmPassword())) {
                return new SignupResponse("Passwords do not match", false, null);
            }
            User user = signupRequestUserMapper.SignupRequestToUser(signupRequest);
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            User savedUser = userRepo.save(user);
            return new SignupResponse("User registered Successfully", true, savedUser.getId());

        } catch (Exception e) {
            return new SignupResponse("Registeration failed", false, null);
        }
    }

    @Override
    @Transactional
    public LoginResponse authenticateUser(LoginRequest loginRequest, String deviceId) {
        try {
            // Authenticate using Spring Security
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String username = userDetails.getUsername();
            String oldRefreshToken = username + ":" + deviceId;
            // SECURITY: Delete existing refresh token for this device (if any)
            Optional<RefreshToken> existingDeviceToken = refreshTokenRepository.findById(oldRefreshToken);
            if (existingDeviceToken.isPresent()) {
                refreshTokenRepository.delete(existingDeviceToken.get());
                logger.info("Replaced existing session for user: {} on device: {}", username, deviceId);
            }

            // Generate new JWT tokens
            String token = jwtUtil.generateToken(userDetails);
            String refreshToken = jwtUtil.generateRefreshToken(userDetails, deviceId);

            // Create access token hash for pairing using SHA-256
            String accessTokenHash = generateTokenHash(token);

            // Save new refresh token to Redis with device ID
            RefreshToken refreshTokenEntity = new RefreshToken();
            refreshTokenEntity.setId(username + ":" + deviceId); // Composite key
            refreshTokenEntity.setToken(refreshToken);
            refreshTokenEntity.setUsername(username);
            refreshTokenEntity.setDeviceId(deviceId);
            refreshTokenEntity.setAccessTokenHash(accessTokenHash);
            refreshTokenEntity.setExpiryDate(jwtUtil.extractExpiration(refreshToken));
            refreshTokenEntity.setCreatedAt(LocalDateTime.now());
            refreshTokenEntity.setLastUsed(LocalDateTime.now());
            refreshTokenRepository.save(refreshTokenEntity);

            logger.info("Issued new tokens for user: {} on device: {}", username, deviceId);

            // Get user details (already fetched earlier)
            User user = userRepo.findByEmail(loginRequest.getEmail()).get();
            LoginResponse.UserInfo userInfo = userLoginResponseUserInfoMapper.UserToUserInfoMapper(user);
            return new LoginResponse("Login successful", true, token, refreshToken, userInfo);

        } catch (Exception e) {
            logger.warn("Login failed for email: {} - {}", loginRequest.getEmail(), e.getMessage());
            return new LoginResponse("Invalid email or password", false, null, null, null);
        }
    }

    @Override
    @Transactional
    public ForgotPasswordResponse forgotPassword(ForgotPasswordRequest request) {
        try {
            Optional<User> optionalUser = userRepo.findByEmail(request.getEmail());

            if (optionalUser.isEmpty()) {
                return new ForgotPasswordResponse(
                        "This Email is not registered",
                        false);
            }

            User user = optionalUser.get();

            String resetToken = UUID.randomUUID().toString();
            user.setResetToken(resetToken);
            user.setResetTokenExpiry(LocalDateTime.now().plusHours(1)); // 1 hour expiry

            userRepo.save(user);

            emailService.sendPasswordResetEmail(
                    user.getEmail(),
                    user.getFirstName(),
                    resetToken);

            return new ForgotPasswordResponse(
                    "If your email is registered, you will receive a password reset link.",
                    true);

        } catch (Exception e) {
            return new ForgotPasswordResponse(
                    "An error occurred while processing your request.",
                    false);
        }
    }

    @Override
    @Transactional
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
                    false);
        }
    }

    /**
     * Generate SHA-256 hash of the access token for pairing with refresh token
     */
    private String generateTokenHash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(token.getBytes(StandardCharsets.UTF_8));

            // Convert byte array to hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            logger.error("SHA-256 algorithm not found", e);
            // Fallback to absolute value of hashCode if SHA-256 fails
            return String.valueOf(Math.abs(token.hashCode()));
        }
    }
}
