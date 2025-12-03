package com.example.gradproject.entity;

import java.time.LocalDateTime;
import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@RedisHash(value = "RefreshToken", timeToLive = 2592000) // 30 days in seconds
public class RefreshToken {
    @Id
    private String id; // Format: "username:deviceId"

    private String token; // The actual refresh token JWT

    private String username; // User's email (no index needed - always use composite key)

    private String deviceId; // Random UUID for device identification

    private String accessTokenHash; // Hash of current access token for pairing

    private Date expiryDate;

    private LocalDateTime createdAt;

    private LocalDateTime lastUsed;
}
