package com.example.gradproject.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.gradproject.DTO.SessionInfo;
import com.example.gradproject.Repository.RefreshTokenRepository;
import com.example.gradproject.entity.RefreshToken;
import com.example.gradproject.service.SessionManagementService;

@Service
public class SessionManagementServiceImpl implements SessionManagementService {

    private static final Logger logger = LoggerFactory.getLogger(SessionManagementServiceImpl.class);

    private final RefreshTokenRepository refreshTokenRepository;

    public SessionManagementServiceImpl(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Override
    public List<SessionInfo> getActiveSessions(String username, String currentDeviceId) {
        List<RefreshToken> tokens = refreshTokenRepository.findByUsername(username);

        return tokens.stream()
                .map(token -> new SessionInfo(
                        token.getDeviceId(),
                        token.getCreatedAt(),
                        token.getLastUsed(),
                        token.getDeviceId().equals(currentDeviceId)))
                .collect(Collectors.toList());
    }

    @Override
    public boolean revokeSession(String username, String deviceId) {
        try {
            // Use composite ID for direct deletion
            String compositeId = username + ":" + deviceId;
            refreshTokenRepository.deleteById(compositeId);
            logger.info("Revoked session for user: {} on device: {} (ID: {})", username, deviceId, compositeId);
            return true;
        } catch (Exception e) {
            logger.error("Error revoking session for user: {} on device: {}", username, deviceId, e);
            return false;
        }
    }

    @Override
    public int revokeAllOtherSessions(String username, String currentDeviceId) {
        List<RefreshToken> tokens = refreshTokenRepository.findByUsername(username);
        int revokedCount = 0;

        for (RefreshToken token : tokens) {
            if (!token.getDeviceId().equals(currentDeviceId)) {
                refreshTokenRepository.delete(token);
                revokedCount++;
            }
        }

        logger.info("Revoked {} other session(s) for user: {}", revokedCount, username);
        return revokedCount;
    }

    @Override
    public int revokeAllSessions(String username) {
        List<RefreshToken> tokens = refreshTokenRepository.findByUsername(username);
        int count = tokens.size();

        tokens.forEach(refreshTokenRepository::delete);

        logger.info("Revoked all {} session(s) for user: {}", count, username);
        return count;
    }
}
