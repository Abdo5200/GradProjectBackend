package com.example.gradproject.service;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

@Service
public class S3Service {

    private static final Logger logger = LoggerFactory.getLogger(S3Service.class);

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucketName}")
    private String bucketName;

    @Value("${aws.region}")
    private String region;


    public S3Service(S3Client s3Client, S3Presigner s3Presigner) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
    }

    /**
     * Upload a single file to S3 (private)
     */
    public String uploadFile(MultipartFile file, String folder) {
        try {
            String fileName = generateFileName(file.getOriginalFilename());
            String key = (folder != null && !folder.isEmpty()) ? folder + fileName : fileName;

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .acl(ObjectCannedACL.PRIVATE)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            // ✅ Generate temporary presigned URL for private access
            String presignedUrl = generatePresignedUrl(key ,Duration.ofMinutes(60));
            logger.info("File uploaded successfully to S3: {}", presignedUrl);

            return presignedUrl;

        } catch (IOException e) {
            logger.error("Error uploading file to S3", e);
            throw new RuntimeException("Error uploading file to S3: " + e.getMessage());
        }
    }

    /**
     * Upload multiple files to S3
     */
    public List<String> uploadFiles(MultipartFile[] files, String folder) {
        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            urls.add(uploadFile(file, folder));
        }
        return urls;
    }
    /**
     * Uploads file to S3 and returns its S3 key (not a presigned URL)
     */
    public String uploadFileAndReturnKey(MultipartFile file, String folder) {
        try {
            String fileName = generateFileName(file.getOriginalFilename());
            String key = (folder != null && !folder.isEmpty()) ? folder + fileName : fileName;

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .acl(ObjectCannedACL.PRIVATE)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            logger.info("✅ File uploaded successfully to S3: {}", key);
            return key; // return the S3 object key

        } catch (IOException e) {
            logger.error("❌ Error uploading file to S3", e);
            throw new RuntimeException("Error uploading file to S3: " + e.getMessage());
        }
    }


    /**
     * Delete a file from S3
     */
    public void deleteFile(String fileUrl) {
        try {
            String key = extractKeyFromUrl(fileUrl);
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build());

            logger.info("File deleted successfully from S3: {}", key);

        } catch (Exception e) {
            logger.error("Error deleting file from S3", e);
            throw new RuntimeException("Error deleting file from S3: " + e.getMessage());
        }
    }


    public String generatePresignedUrl(String key, Duration duration) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(duration)
                .getObjectRequest(getObjectRequest)
                .build();

        return s3Presigner.presignGetObject(presignRequest).url().toString();
    }

    private String generateFileName(String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return UUID.randomUUID() + extension;
    }

    private String extractKeyFromUrl(String fileUrl) {
        String[] parts = fileUrl.split(".amazonaws.com/");
        return parts.length > 1 ? parts[1].split("\\?")[0] : "";
    }
}
