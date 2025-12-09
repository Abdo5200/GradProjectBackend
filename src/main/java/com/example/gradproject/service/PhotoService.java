package com.example.gradproject.service;

import java.time.Duration;
import java.util.Map;

import com.example.gradproject.entity.User;

public interface PhotoService {

    Map<String, Object> getUserPhotos(User user, Duration urlDuration);

    Map<String, String> viewPhoto(String key, Duration duration);

    Map<String, String> deletePhoto(String fileUrl);

    Map<String, String> confirmUpload(String s3Key, User user);
}
