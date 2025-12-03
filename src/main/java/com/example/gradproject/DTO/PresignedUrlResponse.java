package com.example.gradproject.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PresignedUrlResponse {
    private String presignedUrl;
    private String key; // S3 object key
    private String message;
}

