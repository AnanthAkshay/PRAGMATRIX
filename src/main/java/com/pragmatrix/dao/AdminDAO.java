package com.pragmatrix.dao;

import com.pragmatrix.model.Admin;
import com.pragmatrix.util.DBConnection;

import java.sql.*;

/**
 * Data Access Object for the admins table.
 */
public class AdminDAO {

    /**
     * Find an admin by username.
     *
     * @param username the admin username
     * @return Admin object or null if not found
     */
    public Admin findByUsername(String username) throws SQLException {
        String sql = "SELECT admin_id, username, password_hash, full_name, email, created_at FROM admins WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    /**
     * Find an admin by ID.
     */
    public Admin findById(int adminId) throws SQLException {
        String sql = "SELECT admin_id, username, password_hash, full_name, email, created_at FROM admins WHERE admin_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, adminId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    /**
     * Update an admin's password hash.
     */
    public void updatePassword(int adminId, String newHash) throws SQLException {
        String sql = "UPDATE admins SET password_hash = ? WHERE admin_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newHash);
            ps.setInt(2, adminId);
            ps.executeUpdate();
        }
    }

    /**
     * Insert a new admin (used by seed scripts / startup).
     */
    public void insertIfNotExists(String username, String passwordHash, String fullName) throws SQLException {
        String sql = "INSERT IGNORE INTO admins (username, password_hash, full_name) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, passwordHash);
            ps.setString(3, fullName);
            ps.executeUpdate();
        }
    }

    private Admin mapRow(ResultSet rs) throws SQLException {
        Admin a = new Admin();
        a.setAdminId(rs.getInt("admin_id"));
        a.setUsername(rs.getString("username"));
        a.setPasswordHash(rs.getString("password_hash"));
        a.setFullName(rs.getString("full_name"));
        a.setEmail(rs.getString("email"));
        a.setCreatedAt(rs.getTimestamp("created_at"));
        return a;
    }
}
