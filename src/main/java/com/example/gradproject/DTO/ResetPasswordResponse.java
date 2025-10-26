package com.example.gradproject.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ResetPasswordResponse {

    private String message;

    private boolean success;
}
