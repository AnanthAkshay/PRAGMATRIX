package com.pragmatrix.dao;

import com.pragmatrix.model.LeaderboardEntry;
import com.pragmatrix.model.Score;
import com.pragmatrix.util.DBConnection;

import java.sql.*;
import java.util.*;

/**
 * Data Access Object for the scores table and leaderboard queries.
 */
public class ScoreDAO {

    /**
     * Upsert a single score (insert or update on duplicate key).
     */
    public void upsertScore(Score score) throws SQLException {
        String sql = "INSERT INTO scores (unique_id, round_id, points, entered_by) VALUES (?, ?, ?, ?) "
                   + "ON DUPLICATE KEY UPDATE points = VALUES(points), entered_by = VALUES(entered_by)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, score.getUniqueId());
            ps.setInt(2, score.getRoundId());
            ps.setDouble(3, score.getPoints());
            ps.setInt(4, score.getEnteredBy());
            ps.executeUpdate();
        }
    }

    /**
     * Batch upsert scores for multiple teams in a single transaction.
     * Used when admin submits scores for all teams in a round at once.
     */
    public void batchUpsert(List<Score> scores) throws SQLException {
        if (scores == null || scores.isEmpty()) return;

        String sql = "INSERT INTO scores (unique_id, round_id, points, entered_by) VALUES (?, ?, ?, ?) "
                   + "ON DUPLICATE KEY UPDATE points = VALUES(points), entered_by = VALUES(entered_by)";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (Score s : scores) {
                    ps.setString(1, s.getUniqueId());
                    ps.setInt(2, s.getRoundId());
                    ps.setDouble(3, s.getPoints());
                    ps.setInt(4, s.getEnteredBy());
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            conn.commit();
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
     * Find all scores for a specific round, keyed by team unique ID.
     *
     * @return Map of uniqueId → Score
     */
    public Map<String, Score> findByRound(int roundId) throws SQLException {
        Map<String, Score> map = new LinkedHashMap<>();
        String sql = "SELECT score_id, unique_id, round_id, points, entered_by, entered_at "
                   + "FROM scores WHERE round_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roundId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Score s = mapRow(rs);
                    map.put(s.getUniqueId(), s);
                }
            }
        }
        return map;
    }

    /**
     * Find all scores for a specific team across all rounds.
     *
     * @return Map of roundId → Score
     */
    public Map<Integer, Score> findByTeam(String uniqueId) throws SQLException {
        Map<Integer, Score> map = new LinkedHashMap<>();
        String sql = "SELECT score_id, unique_id, round_id, points, entered_by, entered_at "
                   + "FROM scores WHERE unique_id = ? ORDER BY round_id";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, uniqueId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Score s = mapRow(rs);
                    map.put(s.getRoundId(), s);
                }
            }
        }
        return map;
    }

    /**
     * Get the full leaderboard for a quiz with per-round point breakdown.
     * Only includes points from finished rounds in the total.
     */
    public List<LeaderboardEntry> getLeaderboard(String quizCode) throws SQLException {
        // First, get all teams with their total points from the leaderboard view
        // Then separately get per-round scores for breakdown display

        Map<String, LeaderboardEntry> entryMap = new LinkedHashMap<>();

        // Query 1: Get leaderboard totals
        String sql1 = "SELECT unique_id, college_name, team_lead_name, quiz_code, total_points "
                     + "FROM leaderboard WHERE quiz_code = ? ORDER BY total_points DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql1)) {
            ps.setString(1, quizCode);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LeaderboardEntry e = new LeaderboardEntry();
                    e.setUniqueId(rs.getString("unique_id"));
                    e.setCollegeName(rs.getString("college_name"));
                    e.setTeamLeadName(rs.getString("team_lead_name"));
                    e.setQuizCode(rs.getString("quiz_code"));
                    e.setTotalPoints(rs.getDouble("total_points"));
                    entryMap.put(e.getUniqueId(), e);
                }
            }
        }

        // Query 2: Get per-round scores for each team (only finished rounds)
        String sql2 = "SELECT s.unique_id, r.round_number, s.points "
                     + "FROM scores s "
                     + "JOIN rounds r ON s.round_id = r.round_id "
                     + "WHERE r.quiz_code = ? AND r.is_finished = TRUE "
                     + "ORDER BY s.unique_id, r.round_number";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql2)) {
            ps.setString(1, quizCode);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String uid = rs.getString("unique_id");
                    int roundNum = rs.getInt("round_number");
                    double points = rs.getDouble("points");
                    LeaderboardEntry entry = entryMap.get(uid);
                    if (entry != null) {
                        entry.putRoundPoints(roundNum, points);
                    }
                }
            }
        }

        return new ArrayList<>(entryMap.values());
    }

    private Score mapRow(ResultSet rs) throws SQLException {
        Score s = new Score();
        s.setScoreId(rs.getInt("score_id"));
        s.setUniqueId(rs.getString("unique_id"));
        s.setRoundId(rs.getInt("round_id"));
        s.setPoints(rs.getDouble("points"));
        s.setEnteredBy(rs.getInt("entered_by"));
        s.setEnteredAt(rs.getTimestamp("entered_at"));
        return s;
    }
}
