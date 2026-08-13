package com.pragmatrix.dao;

import com.pragmatrix.util.DBConnection;

import java.sql.*;

/**
 * Data Access Object for the team_sessions table.
 * Manages server-side session tracking for teams logged into the Team Dashboard.
 */
public class TeamSessionDAO {

    /**
     * Insert a new team session.
     */
    public void insertSession(String sessionId, String uniqueId, Timestamp expiresAt) throws SQLException {
        String sql = "INSERT INTO team_sessions (session_id, unique_id, expires_at) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sessionId);
            ps.setString(2, uniqueId);
            ps.setTimestamp(3, expiresAt);
            ps.executeUpdate();
        }
    }

    /**
     * Check if a session exists and is not expired.
     * Returns the associated unique_id, or null if invalid/expired.
     */
    public String findValidSession(String sessionId) throws SQLException {
        String sql = "SELECT unique_id FROM team_sessions WHERE session_id = ? AND expires_at > NOW()";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("unique_id");
                }
            }
        }
        return null;
    }

    /**
     * Invalidate (delete) a specific session.
     */
    public void invalidateSession(String sessionId) throws SQLException {
        String sql = "DELETE FROM team_sessions WHERE session_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sessionId);
            ps.executeUpdate();
        }
    }

    /**
     * Invalidate all sessions for a team (cleanup on fresh login).
     */
    public void invalidateAllForTeam(String uniqueId) throws SQLException {
        String sql = "DELETE FROM team_sessions WHERE unique_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, uniqueId);
            ps.executeUpdate();
        }
    }
}
