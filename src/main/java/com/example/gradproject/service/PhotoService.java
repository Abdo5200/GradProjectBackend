package com.example.gradproject.service;

import java.time.Duration;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.example.gradproject.entity.User;

public interface PhotoService {

    Map<String, String> uploadPhoto(MultipartFile file, String folder, User user);

    Map<String, Object> getUserPhotos(User user, Duration urlDuration);

    Map<String, String> viewPhoto(String key, Duration duration);

    Map<String, String> deletePhoto(String fileUrl);

    /**
     * Confirm upload completion after frontend uploads directly to S3.
     * Saves the S3 key to the database.
     */
    Map<String, String> confirmUpload(String s3Key, User user);
}
