package com.example.gradproject.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@RedisHash("RefreshToken")
public class RefreshToken {
    @Id
    private String id;

    @Indexed
    private String token;

    @Indexed
    private String username;

    private Date expiryDate;
}
