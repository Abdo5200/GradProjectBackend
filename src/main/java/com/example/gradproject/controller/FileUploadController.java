package com.example.gradproject.controller;

import com.example.gradproject.Repository.PhotoRepository;
import com.example.gradproject.Repository.UserRepo;
import com.example.gradproject.config.JwtUtil;
import com.example.gradproject.entity.Photo;
import com.example.gradproject.entity.User;
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
    private S3Service s3Service;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private PhotoRepository photoRepository;

    @Autowired
    private JwtUtil jwtUtil;
    /**
     * Upload a single image
     * 
     * @param file   The image file to upload
     * @param folder (optional) The folder path in S3 (default: "images/")
     * @return The uploaded file URL
     */

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", required = false, defaultValue = "images/") String folder,
            @RequestHeader("Authorization") String authHeader) {

        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "File is empty"));
            }

            if (!isImageFile(file)) {
                return ResponseEntity.badRequest().body(Map.of("error", "File must be an image"));
            }

            String token = authHeader.replace("Bearer ", "");
            String userEmail = jwtUtil.extractUsername(token);

            User user = userRepo.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // ✅ Upload file and return its S3 key (not expiring URL)
            String s3Key = s3Service.uploadFileAndReturnKey(file, folder);

            // ✅ Save S3 key in DB
            Photo photo = new Photo();
            photo.setUrl(s3Key); // <-- change Photo entity field name from `url` to `s3Key`
            photo.setUser(user);
            photoRepository.save(photo);

            // ✅ Return fresh presigned URL for immediate use
            String presignedUrl = s3Service.generatePresignedUrl(s3Key , Duration.ofMinutes(60));

            Map<String, String> response = new HashMap<>();
            response.put("url", presignedUrl);
            response.put("message", "✅ File uploaded and saved successfully!");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error uploading file", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error uploading file: " + e.getMessage()));
        }
    }

    /**
     * Upload multiple images
     * 
     * @param files  The image files to upload
     * @param folder (optional) The folder path in S3 (default: "images/")
     * @return List of uploaded file URLs
     */
    @PostMapping("/upload/multiple")
    public ResponseEntity<Map<String, Object>> uploadFiles(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(value = "folder", required = false, defaultValue = "images/") String folder) {

        try {
            // Validate files
            if (files == null || files.length == 0) {
                Map<String, Object> response = new HashMap<>();
                response.put("error", "No files provided");
                return ResponseEntity.badRequest().body(response);
            }

            List<String> urls = s3Service.uploadFiles(files, folder);

            Map<String, Object> response = new HashMap<>();
            response.put("urls", urls);
            response.put("count", urls.size());
            response.put("message", "Files uploaded successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error uploading files", e);
            Map<String, Object> response = new HashMap<>();
            response.put("error", "Error uploading files: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Delete a file from S3
     * 
     * @param fileUrl The URL of the file to delete
     * @return Success message
     */
    @DeleteMapping("/delete")
    public ResponseEntity<Map<String, String>> deleteFile(@RequestParam("url") String fileUrl) {

        try {
            s3Service.deleteFile(fileUrl);

            Map<String, String> response = new HashMap<>();
            response.put("message", "File deleted successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error deleting file", e);
            Map<String, String> response = new HashMap<>();
            response.put("error", "Error deleting file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Check if the uploaded file is an image
     */
    private boolean isImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && (contentType.equals("image/jpeg") ||
                contentType.equals("image/png") ||
                contentType.equals("image/gif") ||
                contentType.equals("image/bmp") ||
                contentType.equals("image/webp"));
    }
 // new method
    @GetMapping("/view")
    public ResponseEntity<Map<String, String>> viewFile(@RequestParam("key") String key) {
        try {
            String presignedUrl = s3Service.generatePresignedUrl(key , Duration.ofMinutes(60));
            return ResponseEntity.ok(Map.of("url", presignedUrl));
        } catch (Exception e) {
            logger.error("Error generating view URL", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error generating URL: " + e.getMessage()));
        }
    }

    @GetMapping("/my-photos")
    public ResponseEntity<?> getMyPhotos(@RequestHeader("Authorization") String token) {
        try {
            // ✅ Remove "Bearer " prefix
            String jwt = token.replace("Bearer ", "");

            // ✅ Extract email (username) from token
            String email = jwtUtil.extractUsername(jwt);

            // ✅ Find the user by email
            User user = userRepo.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // ✅ Get user’s photos
            List<Photo> photos = user.getPhotos();

            // ✅ Convert to list of refreshed presigned URLs
            List<Map<String, String>> photoData = photos.stream()
                    .map(photo -> {
                        // extract key from old URL (e.g., "images/abc.png")

                        String oldUrl = photo.getUrl();
                        String key = extractKeyFromUrl(oldUrl);
                         // String key = photo.getUrl();
                        // generate new presigned URL
                        System.out.println(key);
                        String newUrl = s3Service.generatePresignedUrl(key, Duration.ofMinutes(60));
                        System.out.println(newUrl);
                        return Map.of(
                                "key", key,
                                "url", newUrl
                        );
                    })
                    .toList();

            Map<String, Object> response = new HashMap<>();
            response.put("user", email);
            response.put("count", photoData.size());
            response.put("photos", photoData);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error fetching photos: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * ✅ Helper method to extract S3 key from a full S3 URL
     * e.g. converts:
     *   https://bucketname.s3.eu-central-1.amazonaws.com/images/abc.png?...  →  images/abc.png
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
