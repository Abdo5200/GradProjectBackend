package com.example.gradproject.service;

import java.util.List;

import com.example.gradproject.DTO.SessionInfo;

public interface SessionManagementService {

    /**
     * Get all active sessions for a user
     * 
     * @param username        the user's email
     * @param currentDeviceId the device ID of the requesting device
     * @return list of session information
     */
    List<SessionInfo> getActiveSessions(String username, String currentDeviceId);

    /**
     * Revoke a specific session by device ID
     * 
     * @param username the user's email
     * @param deviceId the device ID to revoke
     * @return true if successful, false otherwise
     */
    boolean revokeSession(String username, String deviceId);

    /**
     * Revoke all sessions except the current one
     * 
     * @param username        the user's email
     * @param currentDeviceId the device ID to keep active
     * @return number of sessions revoked
     */
    int revokeAllOtherSessions(String username, String currentDeviceId);

    /**
     * Revoke all sessions for a user
     * 
     * @param username the user's email
     * @return number of sessions revoked
     */
    int revokeAllSessions(String username);
}
