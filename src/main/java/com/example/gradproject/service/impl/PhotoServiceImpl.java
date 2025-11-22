package com.example.gradproject.service.impl;

import com.example.gradproject.Repository.PhotoRepository;
import com.example.gradproject.entity.Image;
import com.example.gradproject.entity.User;
import com.example.gradproject.service.PhotoService;
import com.example.gradproject.service.S3Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PhotoServiceImpl implements PhotoService {

    private static final Logger logger = LoggerFactory.getLogger(PhotoServiceImpl.class);

    private final S3Service s3Service;
    private final PhotoRepository photoRepository;

    public PhotoServiceImpl(S3Service s3Service, PhotoRepository photoRepository) {
        this.s3Service = s3Service;
        this.photoRepository = photoRepository;
    }

    @Override
    @Transactional
    public Map<String, String> uploadPhoto(MultipartFile file, String folder,
                                           User user) {
        try {
            // Upload file and return its S3 key
            String s3Key = s3Service.uploadFileAndReturnKey(file, folder);

            // Save S3 key in DB
            Image image = new Image();
            image.setUrl(s3Key);
            image.setUser(user);
            photoRepository.save(image);

            // Return fresh presigned URL for immediate use
            String presignedUrl = s3Service.generatePresignedUrl(s3Key, Duration.ofMinutes(60));

            Map<String, String> response = new HashMap<>();
            response.put("url", presignedUrl);
            response.put("message", "File uploaded and saved successfully!");

            return response;

        } catch (Exception e) {
            logger.error("Error uploading photo for user: {}", user.getEmail(), e);
            throw new RuntimeException("Error uploading file: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getUserPhotos(User user, Duration urlDuration) {
        try {
            // Get user's photos
            List<Image> images = user.getImages();

            // Convert to list of refreshed presigned URLs
            List<Map<String, String>> photoData = images.stream()
                    .map(image -> {
                        String oldUrl = image.getUrl();
                        String key = extractKeyFromUrl(oldUrl);
                        String newUrl = s3Service.generatePresignedUrl(key, urlDuration);

                        return Map.of(
                                "key", key,
                                "url", newUrl);
                    })
                    .toList();

            Map<String, Object> response = new HashMap<>();
            response.put("user", user.getEmail());
            response.put("count", photoData.size());
            response.put("photos", photoData);

            return response;

        } catch (Exception e) {
            logger.error("Error fetching photos for user: {}", user.getEmail(), e);
            throw new RuntimeException("Error fetching photos: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, String> viewPhoto(String key, Duration duration) {
        try {
            String presignedUrl = s3Service.generatePresignedUrl(key, duration);
            return Map.of("url", presignedUrl);
        } catch (Exception e) {
            logger.error("Error generating view URL for key: {}", key, e);
            throw new RuntimeException("Error generating URL: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public Map<String, String> deletePhoto(String fileUrl) {
        try {
            s3Service.deleteFile(fileUrl);

            Map<String, String> response = new HashMap<>();
            response.put("message", "File deleted successfully");

            return response;
        } catch (Exception e) {
            logger.error("Error deleting photo", e);
            throw new RuntimeException("Error deleting file: " + e.getMessage(), e);
        }
    }

    /**
     * Helper method to extract S3 key from a full S3 URL
     * e.g. converts:
     * https://bucketname.s3.eu-central-1.amazonaws.com/images/abc.png?... →
     * images/abc.png
     */
    private String extractKeyFromUrl(String fileUrl) {
        if (fileUrl == null || !fileUrl.contains(".amazonaws.com/")) {
            return fileUrl; // fallback, maybe already a key
        }
        String[] parts = fileUrl.split(".amazonaws.com/");
        if (parts.length > 1) {
            String keyPart = parts[1];
            // Remove query params (e.g., ?AWSAccessKeyId=...)
            int qIndex = keyPart.indexOf("?");
            if (qIndex != -1) {
                keyPart = keyPart.substring(0, qIndex);
            }
            return keyPart;
        }
        return fileUrl;
    }
}
