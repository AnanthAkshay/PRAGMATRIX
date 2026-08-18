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

            String sql = "INSERT INTO teams (unique_id, quiz_code, college_name, team_lead_name, lead_email) "
                       + "VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, uniqueId);
                ps.setString(2, team.getQuizCode());
                ps.setString(3, team.getCollegeName());
                ps.setString(4, team.getTeamLeadName());
                ps.setString(5, team.getLeadEmail());
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
        String sql = "SELECT unique_id, quiz_code, college_name, team_lead_name, lead_email, "
                   + "is_eliminated, advanced_to_finale, registered_at "
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
     * Find the lead email for a team by unique ID.
     * Convenience method for email-only lookups (OTP, resend).
     */
    public String findLeadEmailByUniqueId(String uniqueId) throws SQLException {
        String sql = "SELECT lead_email FROM teams WHERE unique_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, uniqueId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("lead_email");
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
        String sql = "SELECT t.unique_id, t.quiz_code, t.college_name, t.team_lead_name, t.lead_email, "
                   + "t.is_eliminated, t.advanced_to_finale, t.registered_at, "
                   + "COALESCE(SUM(CASE WHEN r.is_finished = TRUE THEN s.points ELSE 0 END), 0) AS total_points "
                   + "FROM teams t "
                   + "LEFT JOIN scores s ON t.unique_id = s.unique_id "
                   + "LEFT JOIN rounds r ON s.round_id = r.round_id "
                   + "WHERE t.quiz_code = ? "
                   + "GROUP BY t.unique_id, t.quiz_code, t.college_name, t.team_lead_name, t.lead_email, "
                   + "t.is_eliminated, t.advanced_to_finale, t.registered_at "
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
     * Find only active (non-eliminated) teams for a specific quiz.
     */
    public List<Team> findActiveTeamsByQuizCode(String quizCode) throws SQLException {
        List<Team> list = new ArrayList<>();
        String sql = "SELECT t.unique_id, t.quiz_code, t.college_name, t.team_lead_name, t.lead_email, "
                   + "t.is_eliminated, t.advanced_to_finale, t.registered_at, "
                   + "COALESCE(SUM(CASE WHEN r.is_finished = TRUE THEN s.points ELSE 0 END), 0) AS total_points "
                   + "FROM teams t "
                   + "LEFT JOIN scores s ON t.unique_id = s.unique_id "
                   + "LEFT JOIN rounds r ON s.round_id = r.round_id "
                   + "WHERE t.quiz_code = ? AND t.is_eliminated = FALSE "
                   + "GROUP BY t.unique_id, t.quiz_code, t.college_name, t.team_lead_name, t.lead_email, "
                   + "t.is_eliminated, t.advanced_to_finale, t.registered_at "
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
        String sql = "SELECT t.unique_id, t.quiz_code, t.college_name, t.team_lead_name, t.lead_email, "
                   + "t.is_eliminated, t.advanced_to_finale, t.registered_at, "
                   + "COALESCE(SUM(CASE WHEN r.is_finished = TRUE THEN s.points ELSE 0 END), 0) AS total_points "
                   + "FROM teams t "
                   + "LEFT JOIN scores s ON t.unique_id = s.unique_id "
                   + "LEFT JOIN rounds r ON s.round_id = r.round_id "
                   + "WHERE t.quiz_code = ? AND (t.unique_id LIKE ? OR t.college_name LIKE ?) "
                   + "GROUP BY t.unique_id, t.quiz_code, t.college_name, t.team_lead_name, t.lead_email, "
                   + "t.is_eliminated, t.advanced_to_finale, t.registered_at "
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
     * Update elimination status for a single team.
     */
    public boolean updateEliminationStatus(String uniqueId, boolean isEliminated) throws SQLException {
        String sql = "UPDATE teams SET is_eliminated = ? WHERE unique_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, isEliminated);
            ps.setString(2, uniqueId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Update elimination status for multiple teams in batch.
     */
    public void updateEliminationStatusBatch(List<String> uniqueIds, boolean isEliminated) throws SQLException {
        if (uniqueIds == null || uniqueIds.isEmpty()) return;
        String sql = "UPDATE teams SET is_eliminated = ? WHERE unique_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (String uid : uniqueIds) {
                ps.setBoolean(1, isEliminated);
                ps.setString(2, uid);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    /**
     * Update finale advancement flag for a team.
     */
    public boolean updateFinaleAdvancement(String uniqueId, boolean advanced) throws SQLException {
        String sql = "UPDATE teams SET advanced_to_finale = ? WHERE unique_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, advanced);
            ps.setString(2, uniqueId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Atomically sets the advanced_to_finale flag for selected teams in a quiz,
     * resetting all other teams in the quiz to false.
     */
    public void setGrandFinaleAdvancement(String quizCode, List<String> advancedUniqueIds) throws SQLException {
        String resetSql = "UPDATE teams SET advanced_to_finale = FALSE WHERE quiz_code = ?";
        String advanceSql = "UPDATE teams SET advanced_to_finale = TRUE WHERE unique_id = ?";
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);
            try (PreparedStatement psReset = conn.prepareStatement(resetSql)) {
                psReset.setString(1, quizCode);
                psReset.executeUpdate();
            }
            if (advancedUniqueIds != null && !advancedUniqueIds.isEmpty()) {
                try (PreparedStatement psAdvance = conn.prepareStatement(advanceSql)) {
                    for (String id : advancedUniqueIds) {
                        if (id != null && !id.trim().isEmpty()) {
                            psAdvance.setString(1, id.trim());
                            psAdvance.addBatch();
                        }
                    }
                    psAdvance.executeBatch();
                }
            }
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            throw e;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
                try { conn.close(); } catch (SQLException ignored) {}
            }
        }
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

    /**
     * Delete a team by unique ID. Cascades deletion of all associated scores and session tokens.
     */
    public boolean deleteByUniqueId(String uniqueId) throws SQLException {
        String sql = "DELETE FROM teams WHERE unique_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, uniqueId);
            return ps.executeUpdate() > 0;
        }
    }

    private Team mapRow(ResultSet rs) throws SQLException {
        Team t = new Team();
        t.setUniqueId(rs.getString("unique_id"));
        t.setQuizCode(rs.getString("quiz_code"));
        t.setCollegeName(rs.getString("college_name"));
        t.setTeamLeadName(rs.getString("team_lead_name"));
        t.setLeadEmail(rs.getString("lead_email"));
        t.setRegisteredAt(rs.getTimestamp("registered_at"));
        try {
            t.setEliminated(rs.getBoolean("is_eliminated"));
        } catch (SQLException ignored) {}
        try {
            t.setAdvancedToFinale(rs.getBoolean("advanced_to_finale"));
        } catch (SQLException ignored) {}
        return t;
    }
}
