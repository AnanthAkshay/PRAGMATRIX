package com.pragmatrix.dao;

import com.pragmatrix.model.Admin;
import com.pragmatrix.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for the admins table.
 * Supports up to 10 admin accounts with hashed passwords.
 */
public class AdminDAO {

    public static final int MAX_ADMIN_CAP = 10;

    /**
     * Find an admin by username or email.
     */
    public Admin findByUsernameOrEmail(String identifier) throws SQLException {
        if (identifier == null || identifier.trim().isEmpty()) return null;
        String sql = "SELECT admin_id, username, password_hash, full_name, email, created_at FROM admins WHERE LOWER(username) = LOWER(?) OR LOWER(email) = LOWER(?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, identifier.trim());
            ps.setString(2, identifier.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    /**
     * Find an admin by username.
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
     * Find an admin by email (case-insensitive lookup).
     */
    public Admin findByEmail(String email) throws SQLException {
        if (email == null) return null;
        String sql = "SELECT admin_id, username, password_hash, full_name, email, created_at FROM admins WHERE LOWER(email) = LOWER(?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email.trim());
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
     * Check if an admin exists with the given email.
     */
    public boolean existsByEmail(String email) throws SQLException {
        if (email == null) return false;
        String sql = "SELECT 1 FROM admins WHERE LOWER(email) = LOWER(?) LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Count total registered admins.
     */
    public int count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM admins";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    /**
     * Return all admins ordered by created_at.
     */
    public List<Admin> findAll() throws SQLException {
        List<Admin> list = new ArrayList<>();
        String sql = "SELECT admin_id, username, password_hash, full_name, email, created_at FROM admins ORDER BY admin_id ASC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    /**
     * Insert a new admin, strictly enforcing the maximum 10 admin cap.
     *
     * @param admin Admin object with fullName, email, passwordHash
     * @return true if inserted, false if cap reached or insert failed
     */
    public boolean insert(Admin admin) throws SQLException {
        int currentCount = count();
        if (currentCount >= MAX_ADMIN_CAP) {
            return false;
        }

        String username = admin.getUsername() != null && !admin.getUsername().isEmpty()
                ? admin.getUsername()
                : admin.getEmail();

        String sql = "INSERT INTO admins (username, password_hash, full_name, email) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, username);
            ps.setString(2, admin.getPasswordHash());
            ps.setString(3, admin.getFullName());
            ps.setString(4, admin.getEmail());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        admin.setAdminId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Delete an admin by ID.
     */
    public boolean delete(int adminId) throws SQLException {
        String sql = "DELETE FROM admins WHERE admin_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, adminId);
            return ps.executeUpdate() > 0;
        }
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
     * Insert a new admin if not existing (used by seed scripts / startup).
     */
    public void insertIfNotExists(String username, String passwordHash, String fullName, String email) throws SQLException {
        if (count() >= MAX_ADMIN_CAP) {
            return;
        }
        String sql = "INSERT IGNORE INTO admins (username, password_hash, full_name, email) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username != null ? username : email);
            ps.setString(2, passwordHash);
            ps.setString(3, fullName);
            ps.setString(4, email);
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
