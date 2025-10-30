package com.example.gradproject.service;

import java.util.Map;

public interface AuthService {

    Map<String, String> logout(String token);
}
