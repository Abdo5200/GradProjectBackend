package com.example.gradproject.DTO;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessionInfo {
    private String deviceId;
    private LocalDateTime createdAt;
    private LocalDateTime lastUsed;
    private boolean isCurrentDevice;
}
