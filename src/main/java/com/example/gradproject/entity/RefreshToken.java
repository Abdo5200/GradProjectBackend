package com.example.gradproject.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@RedisHash(value = "RefreshToken", timeToLive = 2592000) // 30 days in seconds
public class RefreshToken {
    @Id
    private String id; // Format: "username:deviceId" - composite key for O(1) lookup

    private String token; // The actual refresh token JWT

    private Date expiryDate; // Token expiration date
}
