package com.example.gradproject.service.impl;

import com.example.gradproject.DTO.LoginRequest;
import com.example.gradproject.DTO.LoginResponse;
import com.example.gradproject.DTO.SignupRequest;
import com.example.gradproject.DTO.SignupResponse;
import com.example.gradproject.Repository.UserRepo;
import com.example.gradproject.config.JwtUtil;
import com.example.gradproject.entity.User;
import com.example.gradproject.exception.UserNotFoundException;
import com.example.gradproject.service.UserService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Autowired
    public UserServiceImpl(UserRepo userRepo, PasswordEncoder passwordEncoder,
                           AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
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
            User user = new User();
            user.setFirstName(signupRequest.getFirstName());
            user.setLastName(signupRequest.getLastName());
            user.setEmail(signupRequest.getEmail());
            user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
            User savedUser = userRepo.save(user);
            return new SignupResponse("User registered Successfully", true, savedUser.getId());

        } catch (Exception e) {
            return new SignupResponse("Registeration failed", false, null);
        }
    }

    @Override
    @Transactional
    public LoginResponse authenticateUser(LoginRequest loginRequest) {
        try {
            // Authenticate using Spring Security
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            // Generate JWT token
            String token = jwtUtil.generateToken(userDetails);

            // Get user details
            Optional<User> optionalUser = userRepo.findByEmail(loginRequest.getEmail());
            if (optionalUser.isEmpty()) {
                return new LoginResponse("User not found", false, null, null);
            }

            User user = optionalUser.get();
            LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo(
                    user.getId(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getEmail());

            return new LoginResponse("Login successful", true, token, userInfo);

        } catch (Exception e) {
            return new LoginResponse("Invalid email or password", false, null, null);
        }
    }
}
