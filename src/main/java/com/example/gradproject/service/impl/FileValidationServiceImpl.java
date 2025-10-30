package com.example.gradproject.service.impl;

import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.gradproject.service.FileValidationService;

@Service
public class FileValidationServiceImpl implements FileValidationService {

    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/bmp",
            "image/webp");

    @Override
    public void validateFileNotEmpty(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
    }

    @Override
    public void validateFileIsImage(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("File must be an image");
        }
    }

    @Override
    public void validateFilesNotEmpty(MultipartFile[] files) {
        if (files == null || files.length == 0) {
            throw new IllegalArgumentException("No files provided");
        }
    }
}
