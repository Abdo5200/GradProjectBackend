package com.example.gradproject.controller;

import com.example.gradproject.service.S3Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
public class FileUploadController {

    private static final Logger logger = LoggerFactory.getLogger(FileUploadController.class);

    @Autowired
    private S3Service s3Service;

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
            @RequestParam(value = "folder", required = false, defaultValue = "images/") String folder) {

        try {
            // Validate file
            if (file.isEmpty()) {
                Map<String, String> response = new HashMap<>();
                response.put("error", "File is empty");
                return ResponseEntity.badRequest().body(response);
            }

            // Validate image type (optional)
            if (!isImageFile(file)) {
                Map<String, String> response = new HashMap<>();
                response.put("error", "File must be an image");
                return ResponseEntity.badRequest().body(response);
            }

            String fileUrl = s3Service.uploadFile(file, folder);

            Map<String, String> response = new HashMap<>();
            response.put("url", fileUrl);
            response.put("message", "File uploaded successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error uploading file", e);
            Map<String, String> response = new HashMap<>();
            response.put("error", "Error uploading file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
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
}
