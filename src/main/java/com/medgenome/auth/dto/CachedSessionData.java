package com.medgenome.auth.dto;


import java.util.ArrayList;
import java.util.List;

/**
 * Cached session data containing sessionId and permissions.
 * Used to maintain session information across token refreshes.
 */
public record CachedSessionData(String sessionId, List<String> permissions, String role, Integer tenantId, Integer userId) {

    public CachedSessionData(String sessionId, List<String> permissions, String role, Integer tenantId, Integer userId) {
        this.sessionId = sessionId;
        this.permissions = permissions != null ? new ArrayList<>(permissions) : new ArrayList<>();
        this.role = role;
        this.tenantId = tenantId;
        this.userId = userId;
    }
}

