package com.example.gradproject.service.impl;

import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.gradproject.Repository.RefreshTokenRepository;
import com.example.gradproject.entity.RefreshToken;

@Service
public class TokenCleanupService {

    private static final Logger logger = Logger.getLogger(TokenCleanupService.class.getName());
    private final RefreshTokenRepository refreshTokenRepository;

    public TokenCleanupService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    /**
     * Scheduled task to clean up expired refresh tokens from Redis
     * Runs every day at 2:00 AM
     */
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

                logger.info("Cleaned up " + count + " expired refresh tokens");
            } else {
                logger.info("No expired tokens to clean up");
            }
        } catch (Exception e) {
            logger.warning("Error during token cleanup: " + e.getMessage());
        }
    }

    /**
     * Manual cleanup method that can be called programmatically
     * 
     * @return Number of tokens cleaned up
     */
    @Transactional
    public int cleanupExpiredTokensManually() {
        try {
            Date now = new Date();
            List<RefreshToken> expiredTokens = refreshTokenRepository.findByExpiryDateBefore(now);

            int count = expiredTokens.size();

            if (count > 0) {
                for (RefreshToken token : expiredTokens) {
                    refreshTokenRepository.delete(token);
                }
                logger.info("Manually cleaned up " + count + " expired refresh tokens");
            }

            return count;
        } catch (Exception e) {
            logger.warning("Error during manual token cleanup: " + e.getMessage());
            return 0;
        }
    }
}
