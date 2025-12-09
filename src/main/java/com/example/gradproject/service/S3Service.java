package com.example.gradproject.service;

import java.time.Duration;

public interface S3Service {

    void deleteFile(String fileUrl);

    void deleteByKey(String key);

    String generatePresignedUrl(String key, Duration duration);

    String generatePresignedPutUrl(String key, String contentType, Duration duration);

    String generateS3Key(String fileName, String folder);
}
