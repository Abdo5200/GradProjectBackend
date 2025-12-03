package com.example.gradproject.entity;

import java.time.LocalDateTime;
import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

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

    @Indexed
    private String token; // The actual refresh token UUID

    @Indexed
    private String username; // User's email

    @Indexed
    private String deviceId; // Random UUID for device identification

    @Indexed
    private String accessTokenHash; // Hash of current access token for pairing

    private Date expiryDate;

    private LocalDateTime createdAt;

    private LocalDateTime lastUsed;
}
