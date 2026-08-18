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
        // First, get all teams with their total points directly from teams, scores, and finished rounds
        // Then separately get per-round scores for breakdown display

        Map<String, LeaderboardEntry> entryMap = new LinkedHashMap<>();

        // Query 1: Get leaderboard totals directly
        String sql1 = "SELECT t.unique_id, t.college_name, t.team_lead_name, t.quiz_code, t.is_eliminated, t.advanced_to_finale, "
                    + "COALESCE(SUM(CASE WHEN r.is_finished = TRUE THEN s.points ELSE 0 END), 0) AS total_points "
                    + "FROM teams t "
                    + "LEFT JOIN scores s ON t.unique_id = s.unique_id "
                    + "LEFT JOIN rounds r ON s.round_id = r.round_id "
                    + "WHERE t.quiz_code = ? "
                    + "GROUP BY t.unique_id, t.college_name, t.team_lead_name, t.quiz_code, t.is_eliminated, t.advanced_to_finale "
                    + "ORDER BY total_points DESC, t.unique_id ASC";
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
                    e.setEliminated(rs.getBoolean("is_eliminated"));
                    e.setAdvanced(rs.getBoolean("advanced_to_finale"));
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

        List<LeaderboardEntry> list = new ArrayList<>(entryMap.values());
        for (int i = 0; i < list.size(); i++) {
            LeaderboardEntry curr = list.get(i);
            if (i > 0) {
                LeaderboardEntry prev = list.get(i - 1);
                if (Double.compare(curr.getTotalPoints(), prev.getTotalPoints()) == 0) {
                    curr.setRank(prev.getRank());
                    curr.setTied(true);
                    prev.setTied(true);
                } else {
                    curr.setRank(i + 1);
                }
            } else {
                curr.setRank(1);
            }
        }
        return list;
    }

    /**
     * Get cumulative ranked standings for a quiz up to a specific round number.
     * Calculates cumulative points across finished rounds <= upToRoundNumber.
     * Calculates competition rank (1, 2, 2, 4...) and flags tied teams.
     *
     * @param quizCode Quiz code (e.g. "BIZWIZX")
     * @param upToRoundNumber Maximum round number to include (e.g. 2 for R1+R2, 3 for R1+R2+R3)
     * @param onlyActive If true, only include non-eliminated teams
     * @return List of ranked LeaderboardEntry items sorted by totalPoints DESC, uniqueId ASC
     */
    public List<LeaderboardEntry> getRankedStandings(String quizCode, int upToRoundNumber, boolean onlyActive) throws SQLException {
        Map<String, LeaderboardEntry> entryMap = new LinkedHashMap<>();

        StringBuilder sql1 = new StringBuilder();
        sql1.append("SELECT t.unique_id, t.college_name, t.team_lead_name, t.quiz_code, t.is_eliminated, ")
            .append("COALESCE(SUM(CASE WHEN r.round_number <= ? AND r.is_finished = TRUE THEN s.points ELSE 0 END), 0) AS total_points ")
            .append("FROM teams t ")
            .append("LEFT JOIN scores s ON t.unique_id = s.unique_id ")
            .append("LEFT JOIN rounds r ON s.round_id = r.round_id ")
            .append("WHERE t.quiz_code = ? ");
        if (onlyActive) {
            sql1.append("AND t.is_eliminated = FALSE ");
        }
        sql1.append("GROUP BY t.unique_id, t.college_name, t.team_lead_name, t.quiz_code, t.is_eliminated ")
            .append("ORDER BY total_points DESC, t.unique_id ASC");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql1.toString())) {
            ps.setInt(1, upToRoundNumber);
            ps.setString(2, quizCode);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LeaderboardEntry e = new LeaderboardEntry();
                    e.setUniqueId(rs.getString("unique_id"));
                    e.setCollegeName(rs.getString("college_name"));
                    e.setTeamLeadName(rs.getString("team_lead_name"));
                    e.setQuizCode(rs.getString("quiz_code"));
                    e.setEliminated(rs.getBoolean("is_eliminated"));
                    e.setTotalPoints(rs.getDouble("total_points"));
                    entryMap.put(e.getUniqueId(), e);
                }
            }
        }

        // Query 2: Get per-round scores for each team up to upToRoundNumber
        StringBuilder sql2 = new StringBuilder();
        sql2.append("SELECT s.unique_id, r.round_number, s.points ")
            .append("FROM scores s ")
            .append("JOIN rounds r ON s.round_id = r.round_id ")
            .append("JOIN teams t ON s.unique_id = t.unique_id ")
            .append("WHERE r.quiz_code = ? AND r.round_number <= ? AND r.is_finished = TRUE ");
        if (onlyActive) {
            sql2.append("AND t.is_eliminated = FALSE ");
        }
        sql2.append("ORDER BY s.unique_id, r.round_number");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql2.toString())) {
            ps.setString(1, quizCode);
            ps.setInt(2, upToRoundNumber);
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

        List<LeaderboardEntry> list = new ArrayList<>(entryMap.values());

        // Assign ranks and detect ties
        for (int i = 0; i < list.size(); i++) {
            LeaderboardEntry curr = list.get(i);
            if (i > 0) {
                LeaderboardEntry prev = list.get(i - 1);
                if (Double.compare(curr.getTotalPoints(), prev.getTotalPoints()) == 0) {
                    curr.setRank(prev.getRank());
                    curr.setTied(true);
                    prev.setTied(true);
                } else {
                    curr.setRank(i + 1);
                }
            } else {
                curr.setRank(1);
            }
        }

        return list;
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
