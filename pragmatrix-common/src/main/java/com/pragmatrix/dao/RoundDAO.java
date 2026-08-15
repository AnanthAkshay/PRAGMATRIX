package com.pragmatrix.dao;

import com.pragmatrix.model.Round;
import com.pragmatrix.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for the rounds table.
 */
public class RoundDAO {

    /**
     * Find all rounds for a quiz, ordered by round number.
     */
    public List<Round> findByQuizCode(String quizCode) throws SQLException {
        List<Round> list = new ArrayList<>();
        String sql = "SELECT round_id, quiz_code, round_number, round_name, judging_criteria, is_finished, finished_at "
                   + "FROM rounds WHERE quiz_code = ? ORDER BY round_number";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, quizCode);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    /**
     * Find a round by its ID.
     */
    public Round findById(int roundId) throws SQLException {
        String sql = "SELECT round_id, quiz_code, round_number, round_name, judging_criteria, is_finished, finished_at "
                   + "FROM rounds WHERE round_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roundId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    /**
     * Update round name and judging criteria.
     * For BIZWIZX: both name and criteria can be updated (if not finished).
     * For VORTEX: only criteria can be updated (name is fixed).
     */
    public void updateRound(int roundId, String roundName, String judgingCriteria) throws SQLException {
        String sql = "UPDATE rounds SET round_name = ?, judging_criteria = ? WHERE round_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, roundName);
            ps.setString(2, judgingCriteria);
            ps.setInt(3, roundId);
            ps.executeUpdate();
        }
    }

    /**
     * Update only the judging criteria for a round (used for VORTEX where names are fixed).
     */
    public void updateCriteria(int roundId, String judgingCriteria) throws SQLException {
        String sql = "UPDATE rounds SET judging_criteria = ? WHERE round_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, judgingCriteria);
            ps.setInt(2, roundId);
            ps.executeUpdate();
        }
    }

    /**
     * Mark a round as finished.
     */
    public void finishRound(int roundId) throws SQLException {
        String sql = "UPDATE rounds SET is_finished = TRUE, finished_at = NOW() WHERE round_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roundId);
            ps.executeUpdate();
        }
    }

    /**
     * Reopen a finished round (for data-correction purposes).
     */
    public void reopenRound(int roundId) throws SQLException {
        String sql = "UPDATE rounds SET is_finished = FALSE, finished_at = NULL WHERE round_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roundId);
            ps.executeUpdate();
        }
    }

    private Round mapRow(ResultSet rs) throws SQLException {
        Round r = new Round();
        r.setRoundId(rs.getInt("round_id"));
        r.setQuizCode(rs.getString("quiz_code"));
        r.setRoundNumber(rs.getInt("round_number"));
        r.setRoundName(rs.getString("round_name"));
        r.setJudgingCriteria(rs.getString("judging_criteria"));
        r.setFinished(rs.getBoolean("is_finished"));
        r.setFinishedAt(rs.getTimestamp("finished_at"));
        return r;
    }
}
