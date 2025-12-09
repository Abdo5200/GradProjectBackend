package com.example.gradproject.service.impl;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.gradproject.Repository.RefreshTokenRepository;
import com.example.gradproject.entity.RefreshToken;
import com.example.gradproject.service.TokenManagementService;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Implementation of TokenManagementService that handles both token extraction
 * and cleanup operations.
 */
@Service
public class TokenManagementServiceImpl implements TokenManagementService {

    private static final Logger logger = LoggerFactory.getLogger(TokenManagementServiceImpl.class);
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final RefreshTokenRepository refreshTokenRepository;

    public TokenManagementServiceImpl(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Override
    public String extractBearerToken(HttpServletRequest request) {
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);
        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            return authHeader.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    /**
     * Scheduled task to clean up expired refresh tokens from Redis
     * Runs every day at 2:00 AM
     */
    @Override
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void cleanupExpiredTokens() {
        try {
            Date now = new Date();
            List<RefreshToken> expiredTokens = refreshTokenRepository.findByExpiryDateBefore(now);

            if (!expiredTokens.isEmpty()) {
                int count = expiredTokens.size();

                // Delete expired tokens
                for (RefreshToken token : expiredTokens) {
                    refreshTokenRepository.delete(token);
                }

                logger.info("Cleaned up {} expired refresh tokens", count);
            } else {
                logger.info("No expired tokens to clean up");
            }
        } catch (Exception e) {
            logger.error("Error during token cleanup: {}", e.getMessage(), e);
        }
    }
}
