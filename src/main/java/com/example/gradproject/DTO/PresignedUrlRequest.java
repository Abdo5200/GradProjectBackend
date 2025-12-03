package com.example.gradproject.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PresignedUrlRequest {
    private String fileName;
    private String contentType;
    private String folder; // Optional, defaults to "images/"
}

