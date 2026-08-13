package com.pragmatrix.dao;

import com.pragmatrix.model.TeamLoginOtp;
import com.pragmatrix.util.DBConnection;

import java.sql.*;

/**
 * Data Access Object for the team_login_otps table.
 * Handles OTP creation, verification, rate-limiting, and cleanup.
 */
public class OtpDAO {

    /**
     * Insert a new OTP for a team.
     */
    public void insertOtp(String uniqueId, String otpCode, Timestamp expiresAt) throws SQLException {
        String sql = "INSERT INTO team_login_otps (unique_id, otp_code, expires_at) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, uniqueId);
            ps.setString(2, otpCode);
            ps.setTimestamp(3, expiresAt);
            ps.executeUpdate();
        }
    }

    /**
     * Find the latest unused, non-expired OTP for a team.
     * Returns null if none found.
     */
    public TeamLoginOtp findLatestUnusedOtp(String uniqueId) throws SQLException {
        String sql = "SELECT otp_id, unique_id, otp_code, generated_at, expires_at, is_used, attempt_count "
                   + "FROM team_login_otps "
                   + "WHERE unique_id = ? AND is_used = FALSE AND expires_at > NOW() "
                   + "ORDER BY generated_at DESC LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, uniqueId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    /**
     * Mark an OTP as used.
     */
    public void markUsed(int otpId) throws SQLException {
        String sql = "UPDATE team_login_otps SET is_used = TRUE WHERE otp_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, otpId);
            ps.executeUpdate();
        }
    }

    /**
     * Increment the wrong-entry attempt counter for an OTP.
     */
    public void incrementAttemptCount(int otpId) throws SQLException {
        String sql = "UPDATE team_login_otps SET attempt_count = attempt_count + 1 WHERE otp_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, otpId);
            ps.executeUpdate();
        }
    }

    /**
     * Count OTPs generated for a team within the last N minutes.
     * Used for rate-limiting (max 5 OTPs per 15 minutes).
     */
    public int countRecentOtps(String uniqueId, int minutesWindow) throws SQLException {
        String sql = "SELECT COUNT(*) FROM team_login_otps "
                   + "WHERE unique_id = ? AND generated_at > DATE_SUB(NOW(), INTERVAL ? MINUTE)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, uniqueId);
            ps.setInt(2, minutesWindow);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    /**
     * Invalidate (mark as used) all existing OTPs for a team.
     * Called before issuing a new OTP to ensure only one active OTP at a time.
     */
    public void invalidateAllForTeam(String uniqueId) throws SQLException {
        String sql = "UPDATE team_login_otps SET is_used = TRUE WHERE unique_id = ? AND is_used = FALSE";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, uniqueId);
            ps.executeUpdate();
        }
    }

    private TeamLoginOtp mapRow(ResultSet rs) throws SQLException {
        TeamLoginOtp otp = new TeamLoginOtp();
        otp.setOtpId(rs.getInt("otp_id"));
        otp.setUniqueId(rs.getString("unique_id"));
        otp.setOtpCode(rs.getString("otp_code"));
        otp.setGeneratedAt(rs.getTimestamp("generated_at"));
        otp.setExpiresAt(rs.getTimestamp("expires_at"));
        otp.setUsed(rs.getBoolean("is_used"));
        otp.setAttemptCount(rs.getInt("attempt_count"));
        return otp;
    }
}
