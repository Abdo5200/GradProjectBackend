package com.example.gradproject.controller;

import java.time.Duration;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.gradproject.DTO.PresignedUrlRequest;
import com.example.gradproject.DTO.PresignedUrlResponse;
import com.example.gradproject.DTO.UploadCompleteRequest;
import com.example.gradproject.Repository.UserRepo;
import com.example.gradproject.entity.User;
import com.example.gradproject.exception.UserNotFoundException;
import com.example.gradproject.service.PhotoService;
import com.example.gradproject.service.S3Service;

@RestController
@RequestMapping("/api/files")
public class FileUploadController {

    private final PhotoService photoService;
    private final S3Service s3Service;
    private final UserRepo userRepo;

    public FileUploadController(
            PhotoService photoService,
            S3Service s3Service,
            UserRepo userRepo) {
        this.photoService = photoService;
        this.s3Service = s3Service;
        this.userRepo = userRepo;
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Map<String, String>> deleteFile(@RequestParam("url") String fileUrl) {
        Map<String, String> response = photoService.deletePhoto(fileUrl);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/view")
    public ResponseEntity<Map<String, String>> viewFile(@RequestParam("key") String key) {
        Map<String, String> response = photoService.viewPhoto(key, Duration.ofMinutes(60));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-photos")
    public ResponseEntity<?> getMyPhotos(Authentication authentication) {
        // Get authenticated user
        String userEmail = authentication.getName();
        User user = userRepo.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // Delegate to photo service
        Map<String, Object> response = photoService.getUserPhotos(user, Duration.ofMinutes(60));
        return ResponseEntity.ok(response);
    }

    /**
     * Generate presigned PUT URL for direct upload to S3.
     * Frontend calls this endpoint first to get a presigned URL, then uploads
     * directly to S3.
     * 
     * @param request        Contains fileName, contentType, and optional folder
     * @param authentication Spring Security authentication object (automatically
     *                       injected)
     * @return Presigned PUT URL and S3 key
     */
    @PostMapping("/presigned-url")
    public ResponseEntity<PresignedUrlResponse> generatePresignedUrl(
            @RequestBody PresignedUrlRequest request,
            Authentication authentication) {

        // Validate request
        if (request.getFileName() == null || request.getFileName().isEmpty()) {
            throw new IllegalArgumentException("File name is required");
        }
        if (request.getContentType() == null || request.getContentType().isEmpty()) {
            throw new IllegalArgumentException("Content type is required");
        }

        // Set default folder if not provided
        String folder = request.getFolder();
        if (folder == null || folder.isEmpty()) {
            folder = "images/";
        }
        // Ensure folder ends with /
        if (!folder.endsWith("/")) {
            folder += "/";
        }

        // Generate unique S3 key
        String s3Key = s3Service.generateS3Key(request.getFileName(), folder);

        // Generate presigned PUT URL (valid for 5 minutes)
        String presignedUrl = s3Service.generatePresignedPutUrl(
                s3Key,
                request.getContentType(),
                Duration.ofMinutes(5));

        PresignedUrlResponse response = new PresignedUrlResponse();
        response.setPresignedUrl(presignedUrl);
        response.setKey(s3Key);
        response.setMessage("Presigned URL generated successfully");

        return ResponseEntity.ok(response);
    }

    /**
     * Confirm upload completion after frontend uploads directly to S3.
     * This endpoint saves the S3 key to the database.
     * 
     * @param request        Contains the S3 key
     * @param authentication Spring Security authentication object (automatically
     *                       injected)
     * @return Confirmation response with presigned GET URL
     */
    @PostMapping("/upload-complete")
    public ResponseEntity<Map<String, String>> confirmUpload(
            @RequestBody UploadCompleteRequest request,
            Authentication authentication) {

        // Get authenticated user
        String userEmail = authentication.getName();
        User user = userRepo.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // Validate request
        if (request.getKey() == null || request.getKey().isEmpty()) {
            throw new IllegalArgumentException("S3 key is required");
        }

        // Confirm upload and save to database
        Map<String, String> response = photoService.confirmUpload(request.getKey(), user);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/analyze")
    public String postMethodName(@RequestBody String entity) {
        // TODO: process POST request

        return entity;
    }

}
