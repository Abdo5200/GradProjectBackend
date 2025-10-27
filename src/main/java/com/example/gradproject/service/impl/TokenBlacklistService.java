package com.example.gradproject.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class TokenBlacklistService {

    private static final Logger logger = LoggerFactory.getLogger(TokenBlacklistService.class);

    private final ConcurrentHashMap<String, Long> blacklistedTokens = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public TokenBlacklistService() {
        // Clean up expired tokens every hour
        scheduler.scheduleAtFixedRate(this::cleanupExpiredTokens, 1, 1, TimeUnit.HOURS);
    }

    public void blacklistToken(String token, long expirationTime) {
        blacklistedTokens.put(token, expirationTime);
        logger.info("Token blacklisted. Map size: {}", blacklistedTokens.size());
    }

    public boolean isTokenBlacklisted(String token) {
        boolean isBlacklisted = blacklistedTokens.containsKey(token);
        logger.debug("Token blacklist check: {} - Result: {}", token.substring(0, Math.min(10, token.length())), isBlacklisted);
        return isBlacklisted;
    }

    private void cleanupExpiredTokens() {
        long currentTime = System.currentTimeMillis();
        blacklistedTokens.entrySet().removeIf(entry -> entry.getValue() < currentTime);
    }

    public void shutdown() {
        scheduler.shutdown();
    }
}
