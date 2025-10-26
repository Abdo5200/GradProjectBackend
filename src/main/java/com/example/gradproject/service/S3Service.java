package com.example.gradproject.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
public class S3Service {

    private static final Logger logger = LoggerFactory.getLogger(S3Service.class);

    @Autowired
    private S3Client s3Client;

    @Value("${aws.s3.bucketName}")
    private String bucketName;

    /**
     * Upload a single file to S3
     * 
     * @param file   The file to upload
     * @param folder The folder path in the bucket (e.g., "images/", "documents/")
     * @return The URL of the uploaded file
     */
    public String uploadFile(MultipartFile file, String folder) {
        try {
            String fileName = generateFileName(file.getOriginalFilename());
            String key = folder != null && !folder.isEmpty() ? folder + fileName : fileName;

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            String fileUrl = String.format("https://%s.s3.%s.amazonaws.com/%s",
                    bucketName, s3Client.serviceName().toString(), key);

            logger.info("File uploaded successfully to S3: {}", fileUrl);
            return fileUrl;

        } catch (Exception e) {
            logger.error("Error uploading file to S3", e);
            throw new RuntimeException("Error uploading file to S3: " + e.getMessage());
        }
    }

    /**
     * Upload multiple files to S3
     * 
     * @param files  The files to upload
     * @param folder The folder path in the bucket
     * @return List of uploaded file URLs
     */
    public List<String> uploadFiles(MultipartFile[] files, String folder) {
        List<String> urls = new ArrayList<>();

        for (MultipartFile file : files) {
            String url = uploadFile(file, folder);
            urls.add(url);
        }

        return urls;
    }

    /**
     * Delete a file from S3
     * 
     * @param fileUrl The URL of the file to delete
     */
    public void deleteFile(String fileUrl) {
        try {
            // Extract key from URL
            String key = extractKeyFromUrl(fileUrl);

            s3Client.deleteObject(builder -> builder.bucket(bucketName)
                    .key(key)
                    .build());

            logger.info("File deleted successfully from S3: {}", key);

        } catch (Exception e) {
            logger.error("Error deleting file from S3", e);
            throw new RuntimeException("Error deleting file from S3: " + e.getMessage());
        }
    }

    /**
     * Generate a unique file name
     */
    private String generateFileName(String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return UUID.randomUUID().toString() + extension;
    }

    /**
     * Extract S3 key from URL
     */
    private String extractKeyFromUrl(String fileUrl) {
        // Extract key from URL like: https://bucketname.s3.region.amazonaws.com/key
        String[] parts = fileUrl.split(".amazonaws.com/");
        return parts.length > 1 ? parts[1] : "";
    }
}
