package com.example.gradproject.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SignupResponse {
    private String message;
    private boolean success;
    private Integer userId;

}
