package com.pragmatrix.dao;

import com.pragmatrix.model.Team;
import com.pragmatrix.util.DBConnection;
import com.pragmatrix.util.IdGenerator;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for the teams table.
 * Handles registration with transaction-safe unique ID generation.
 */
public class TeamDAO {

    /**
     * Register a new team. Generates a unique ID within a transaction.
     *
     * @param team the team to register (uniqueId will be set on return)
     * @param idPrefix the ID prefix for the quiz (e.g. "PMBZ" or "PMVX")
     * @return the generated unique ID
     * @throws SQLException if a database error occurs
     */
    public String insert(Team team, String idPrefix) throws SQLException {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // Generate the next unique ID within the transaction (locked)
            String uniqueId = IdGenerator.generateNextId(conn, idPrefix);
            team.setUniqueId(uniqueId);

            String sql = "INSERT INTO teams (unique_id, quiz_code, college_name, student1_name, student2_name, student3_name) "
                       + "VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, uniqueId);
                ps.setString(2, team.getQuizCode());
                ps.setString(3, team.getCollegeName());
                ps.setString(4, team.getStudent1Name());
                ps.setString(5, team.getStudent2Name());
                ps.setString(6, team.getStudent3Name());
                ps.executeUpdate();
            }

            conn.commit();
            return uniqueId;

        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { /* ignore */ }
            }
            throw e;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException ex) { /* ignore */ }
                try { conn.close(); } catch (SQLException ex) { /* ignore */ }
            }
        }
    }

    /**
     * Find a team by its unique ID.
     */
    public Team findByUniqueId(String uniqueId) throws SQLException {
        String sql = "SELECT unique_id, quiz_code, college_name, student1_name, student2_name, student3_name, registered_at "
                   + "FROM teams WHERE unique_id = ?";
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
     * Find all teams for a specific quiz, ordered by unique ID.
     */
    public List<Team> findByQuizCode(String quizCode) throws SQLException {
        List<Team> list = new ArrayList<>();
        String sql = "SELECT t.unique_id, t.quiz_code, t.college_name, t.student1_name, t.student2_name, t.student3_name, t.registered_at, "
                   + "COALESCE(SUM(CASE WHEN r.is_finished = TRUE THEN s.points ELSE 0 END), 0) AS total_points "
                   + "FROM teams t "
                   + "LEFT JOIN scores s ON t.unique_id = s.unique_id "
                   + "LEFT JOIN rounds r ON s.round_id = r.round_id "
                   + "WHERE t.quiz_code = ? "
                   + "GROUP BY t.unique_id, t.quiz_code, t.college_name, t.student1_name, t.student2_name, t.student3_name, t.registered_at "
                   + "ORDER BY t.unique_id";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, quizCode);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Team t = mapRow(rs);
                    t.setTotalPoints(rs.getDouble("total_points"));
                    list.add(t);
                }
            }
        }
        return list;
    }

    /**
     * Search teams by unique ID or college name (partial match).
     */
    public List<Team> searchTeams(String quizCode, String query) throws SQLException {
        List<Team> list = new ArrayList<>();
        String sql = "SELECT t.unique_id, t.quiz_code, t.college_name, t.student1_name, t.student2_name, t.student3_name, t.registered_at, "
                   + "COALESCE(SUM(CASE WHEN r.is_finished = TRUE THEN s.points ELSE 0 END), 0) AS total_points "
                   + "FROM teams t "
                   + "LEFT JOIN scores s ON t.unique_id = s.unique_id "
                   + "LEFT JOIN rounds r ON s.round_id = r.round_id "
                   + "WHERE t.quiz_code = ? AND (t.unique_id LIKE ? OR t.college_name LIKE ?) "
                   + "GROUP BY t.unique_id, t.quiz_code, t.college_name, t.student1_name, t.student2_name, t.student3_name, t.registered_at "
                   + "ORDER BY t.unique_id";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, quizCode);
            String wildcard = "%" + query + "%";
            ps.setString(2, wildcard);
            ps.setString(3, wildcard);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Team t = mapRow(rs);
                    t.setTotalPoints(rs.getDouble("total_points"));
                    list.add(t);
                }
            }
        }
        return list;
    }

    /**
     * Count teams registered for a quiz.
     */
    public int countByQuizCode(String quizCode) throws SQLException {
        String sql = "SELECT COUNT(*) FROM teams WHERE quiz_code = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, quizCode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    private Team mapRow(ResultSet rs) throws SQLException {
        Team t = new Team();
        t.setUniqueId(rs.getString("unique_id"));
        t.setQuizCode(rs.getString("quiz_code"));
        t.setCollegeName(rs.getString("college_name"));
        t.setStudent1Name(rs.getString("student1_name"));
        t.setStudent2Name(rs.getString("student2_name"));
        t.setStudent3Name(rs.getString("student3_name"));
        t.setRegisteredAt(rs.getTimestamp("registered_at"));
        return t;
    }
}
