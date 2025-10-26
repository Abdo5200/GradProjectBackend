package com.example.gradproject.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {
    private String message;
    private boolean success;
    private String token;
    private UserInfo userInfo;

    @Data
    @AllArgsConstructor
    public static class UserInfo {
        private int id;
        private String firstName;
        private String lastName;
        private String email;
    }
}
