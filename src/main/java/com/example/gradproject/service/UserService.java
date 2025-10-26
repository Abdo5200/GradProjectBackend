package com.example.gradproject.service;

import com.example.gradproject.DTO.LoginRequest;
import com.example.gradproject.DTO.LoginResponse;
import com.example.gradproject.DTO.SignupRequest;
import com.example.gradproject.DTO.SignupResponse;
import com.example.gradproject.entity.User;

public interface UserService {

    User findById(Integer id);

    SignupResponse registerUser(SignupRequest signupRequest);

    LoginResponse authenticateUser(LoginRequest loginRequest);
}
