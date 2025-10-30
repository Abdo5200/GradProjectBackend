package com.example.gradproject.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileValidationService {

    void validateFileNotEmpty(MultipartFile file);

    void validateFileIsImage(MultipartFile file);

    void validateFilesNotEmpty(MultipartFile[] files);
}
