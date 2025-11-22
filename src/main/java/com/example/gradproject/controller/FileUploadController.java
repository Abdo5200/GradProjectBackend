package com.example.gradproject.controller;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.gradproject.Repository.UserRepo;
import com.example.gradproject.config.JwtUtil;
import com.example.gradproject.entity.User;
import com.example.gradproject.exception.UserNotFoundException;
import com.example.gradproject.service.FileValidationService;
import com.example.gradproject.service.PhotoService;
import com.example.gradproject.service.S3Service;

@RestController
@RequestMapping("/api/files")
public class FileUploadController {

    private final PhotoService photoService;
    private final S3Service s3Service;
    private final FileValidationService fileValidationService;
    private final UserRepo userRepo;
    private final JwtUtil jwtUtil;

    public FileUploadController(
            PhotoService photoService,
            S3Service s3Service,
            FileValidationService fileValidationService,
            UserRepo userRepo,
            JwtUtil jwtUtil) {
        this.photoService = photoService;
        this.fileValidationService = fileValidationService;
        this.s3Service = s3Service;
        this.userRepo = userRepo;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", required = false, defaultValue = "images/") String folder,
            @RequestHeader("Authorization") String authHeader) {

        // Validate file
        fileValidationService.validateFileNotEmpty(file);
        fileValidationService.validateFileIsImage(file);

        // Extract user from token
        String token = authHeader.replace("Bearer ", "");
        String userEmail = jwtUtil.extractUsername(token);
        User user = userRepo.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // Delegate to photo service
        Map<String, String> response = photoService.uploadPhoto(file, folder, user);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/upload/multiple")
    public ResponseEntity<Map<String, Object>> uploadFiles(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(value = "folder", required = false, defaultValue = "images/") String folder) {

        // Validate files
        fileValidationService.validateFilesNotEmpty(files);

        // Delegate to S3 service
        List<String> urls = s3Service.uploadFiles(files, folder);

        Map<String, Object> response = new HashMap<>();
        response.put("urls", urls);
        response.put("count", urls.size());
        response.put("message", "Files uploaded successfully");

        return ResponseEntity.ok(response);
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
    public ResponseEntity<?> getMyPhotos(@RequestHeader("Authorization") String token) {
        // Extract user from token
        String jwt = token.replace("Bearer ", "");
        String email = jwtUtil.extractUsername(jwt);
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // Delegate to photo service
        Map<String, Object> response = photoService.getUserPhotos(user, Duration.ofMinutes(60));
        return ResponseEntity.ok(response);
    }

}
