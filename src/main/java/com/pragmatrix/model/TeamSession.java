package com.pragmatrix.model;

import java.sql.Timestamp;

/**
 * Represents a server-side session for a team logged into the Team Dashboard.
 */
public class TeamSession {
    private String sessionId;
    private String uniqueId;
    private Timestamp createdAt;
    private Timestamp expiresAt;

    public TeamSession() {}

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getUniqueId() { return uniqueId; }
    public void setUniqueId(String uniqueId) { this.uniqueId = uniqueId; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Timestamp expiresAt) { this.expiresAt = expiresAt; }
}
