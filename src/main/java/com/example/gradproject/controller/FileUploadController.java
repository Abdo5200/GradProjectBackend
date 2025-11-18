package com.example.gradproject.controller;

import com.example.gradproject.Repository.UserRepo;
import com.example.gradproject.config.JwtUtil;
import com.example.gradproject.entity.User;
import com.example.gradproject.service.FileValidationService;
import com.example.gradproject.service.PhotoService;
import com.example.gradproject.service.S3Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
public class FileUploadController {

    private static final Logger logger = LoggerFactory.getLogger(FileUploadController.class);

    @Autowired
    private PhotoService photoService;

    @Autowired
    private S3Service s3Service;

    @Autowired
    private FileValidationService fileValidationService;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", required = false, defaultValue = "images/") String folder,
            @RequestHeader("Authorization") String authHeader) {

        try {
            // Validate file
            fileValidationService.validateFileNotEmpty(file);
            fileValidationService.validateFileIsImage(file);

            // Extract user from token
            String token = authHeader.replace("Bearer ", "");
            String userEmail = jwtUtil.extractUsername(token);
            User user = userRepo.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Delegate to photo service
            Map<String, String> response = photoService.uploadPhoto(file, folder, user);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            logger.error("Validation error uploading file", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error uploading file", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error uploading file: " + e.getMessage()));
        }
    }

    @PostMapping("/upload/multiple")
    public ResponseEntity<Map<String, Object>> uploadFiles(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(value = "folder", required = false, defaultValue = "images/") String folder) {

        try {
            // Validate files
            fileValidationService.validateFilesNotEmpty(files);

            // Delegate to S3 service
            List<String> urls = s3Service.uploadFiles(files, folder);

            Map<String, Object> response = new HashMap<>();
            response.put("urls", urls);
            response.put("count", urls.size());
            response.put("message", "Files uploaded successfully");

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            logger.error("Validation error uploading files", e);
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            logger.error("Error uploading files", e);
            Map<String, Object> response = new HashMap<>();
            response.put("error", "Error uploading files: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Map<String, String>> deleteFile(@RequestParam("url") String fileUrl) {
        try {
            Map<String, String> response = photoService.deletePhoto(fileUrl);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error deleting file", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error deleting file: " + e.getMessage()));
        }
    }

    @GetMapping("/view")
    public ResponseEntity<Map<String, String>> viewFile(@RequestParam("key") String key) {
        try {
            Map<String, String> response = photoService.viewPhoto(key, Duration.ofMinutes(60));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error generating view URL", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error generating URL: " + e.getMessage()));
        }
    }

    @GetMapping("/my-photos")
    public ResponseEntity<?> getMyPhotos(@RequestHeader("Authorization") String token) {
        try {
            // Extract user from token
            String jwt = token.replace("Bearer ", "");
            String email = jwtUtil.extractUsername(jwt);
            User user = userRepo.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Delegate to photo service
            Map<String, Object> response = photoService.getUserPhotos(user, Duration.ofMinutes(60));
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error fetching photos", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error fetching photos: " + e.getMessage()));
        }
    }

}
