package com.example.gradproject.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ForgotPasswordResponse {
    private String message;

    private boolean success;
}
