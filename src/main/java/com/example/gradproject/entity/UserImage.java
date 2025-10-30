package com.example.gradproject.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_images", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "s3_key"}))
public class UserImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // ✅ Separate primary key

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "s3_key", nullable = false, unique = true)
    private String s3Key;  // "images/abc123.jpg"

    @Column(name = "original_filename")
    private String originalFilename;  // Can be duplicate (e.g., "report.jpg")

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "upload_date")
    private LocalDateTime uploadDate;

    @Column(name = "file_size")
    private Long fileSize;
    
}