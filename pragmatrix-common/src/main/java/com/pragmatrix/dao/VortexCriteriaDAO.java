package com.pragmatrix.dao;

import com.pragmatrix.model.JudgingComponent;
import com.pragmatrix.model.JudgingCriterion;
import com.pragmatrix.model.TeamRoundScore;
import com.pragmatrix.model.VortexRound;
import com.pragmatrix.util.DBConnection;

import java.sql.*;
import java.util.*;

public class VortexCriteriaDAO {

    public List<VortexRound> getAllRounds() {
        List<VortexRound> list = new ArrayList<>();
        String sql = "SELECT * FROM vortex_rounds ORDER BY display_order ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                VortexRound round = new VortexRound(
                        rs.getInt("round_id"),
                        rs.getString("round_name"),
                        rs.getInt("display_order")
                );
                round.setComponents(getComponentsForRound(round.getRoundId(), conn));
                list.add(round);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public VortexRound getRoundById(int roundId) {
        String sql = "SELECT * FROM vortex_rounds WHERE round_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, roundId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    VortexRound round = new VortexRound(
                            rs.getInt("round_id"),
                            rs.getString("round_name"),
                            rs.getInt("display_order")
                    );
                    round.setComponents(getComponentsForRound(round.getRoundId(), conn));
                    return round;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public VortexRound getRoundByDisplayOrder(int displayOrder) {
        String sql = "SELECT * FROM vortex_rounds WHERE display_order = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, displayOrder);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    VortexRound round = new VortexRound(
                            rs.getInt("round_id"),
                            rs.getString("round_name"),
                            rs.getInt("display_order")
                    );
                    round.setComponents(getComponentsForRound(round.getRoundId(), conn));
                    return round;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public VortexRound getRoundByName(String roundName) {
        String sql = "SELECT * FROM vortex_rounds WHERE LOWER(round_name) = LOWER(?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, roundName != null ? roundName.trim() : "");
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    VortexRound round = new VortexRound(
                            rs.getInt("round_id"),
                            rs.getString("round_name"),
                            rs.getInt("display_order")
                    );
                    round.setComponents(getComponentsForRound(round.getRoundId(), conn));
                    return round;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private List<JudgingComponent> getComponentsForRound(int roundId, Connection conn) throws SQLException {
        List<JudgingComponent> list = new ArrayList<>();
        String sql = "SELECT * FROM judging_components WHERE round_id = ? ORDER BY display_order ASC, component_id ASC";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, roundId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    JudgingComponent comp = new JudgingComponent(
                            rs.getInt("component_id"),
                            rs.getInt("round_id"),
                            rs.getString("component_label"),
                            rs.getInt("max_marks"),
                            rs.getInt("display_order")
                    );
                    comp.setCriteria(getCriteriaForComponent(comp.getComponentId(), conn));
                    list.add(comp);
                }
            }
        }
        return list;
    }

    private List<JudgingCriterion> getCriteriaForComponent(int componentId, Connection conn) throws SQLException {
        List<JudgingCriterion> list = new ArrayList<>();
        String sql = "SELECT * FROM judging_criteria WHERE component_id = ? ORDER BY display_order ASC, criterion_id ASC";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, componentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new JudgingCriterion(
                            rs.getInt("criterion_id"),
                            rs.getInt("component_id"),
                            rs.getString("criterion_name"),
                            rs.getString("judges_look_for"),
                            rs.getInt("max_marks"),
                            rs.getInt("display_order")
                    ));
                }
            }
        }
        return list;
    }

    public boolean addComponent(int roundId, String componentLabel, int displayOrder) {
        String sql = "INSERT INTO judging_components (round_id, component_label, max_marks, display_order) VALUES (?, ?, 0, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, roundId);
            stmt.setString(2, componentLabel);
            stmt.setInt(3, displayOrder);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateComponent(int componentId, String componentLabel) {
        String sql = "UPDATE judging_components SET component_label = ? WHERE component_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, componentLabel);
            stmt.setInt(2, componentId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteComponent(int componentId) {
        String sql = "DELETE FROM judging_components WHERE component_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, componentId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean addCriterion(int componentId, String criterionName, String judgesLookFor, int maxMarks, int displayOrder) {
        String sql = "INSERT INTO judging_criteria (component_id, criterion_name, judges_look_for, max_marks, display_order) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, componentId);
            stmt.setString(2, criterionName);
            stmt.setString(3, (judgesLookFor != null && !judgesLookFor.trim().isEmpty()) ? judgesLookFor.trim() : null);
            stmt.setInt(4, maxMarks);
            stmt.setInt(5, displayOrder);
            boolean ok = stmt.executeUpdate() > 0;
            if (ok) {
                recalculateComponentMaxMarks(componentId, conn);
            }
            return ok;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateCriterion(int criterionId, String criterionName, String judgesLookFor, int maxMarks) {
        String selectSql = "SELECT component_id FROM judging_criteria WHERE criterion_id = ?";
        String updateSql = "UPDATE judging_criteria SET criterion_name = ?, judges_look_for = ?, max_marks = ? WHERE criterion_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
            selectStmt.setInt(1, criterionId);
            int compId = 0;
            try (ResultSet rs = selectStmt.executeQuery()) {
                if (rs.next()) compId = rs.getInt("component_id");
            }
            if (compId == 0) return false;

            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                updateStmt.setString(1, criterionName);
                updateStmt.setString(2, (judgesLookFor != null && !judgesLookFor.trim().isEmpty()) ? judgesLookFor.trim() : null);
                updateStmt.setInt(3, maxMarks);
                updateStmt.setInt(4, criterionId);
                boolean ok = updateStmt.executeUpdate() > 0;
                if (ok) {
                    recalculateComponentMaxMarks(compId, conn);
                }
                return ok;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteCriterion(int criterionId) {
        String selectSql = "SELECT component_id FROM judging_criteria WHERE criterion_id = ?";
        String deleteSql = "DELETE FROM judging_criteria WHERE criterion_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
            selectStmt.setInt(1, criterionId);
            int compId = 0;
            try (ResultSet rs = selectStmt.executeQuery()) {
                if (rs.next()) compId = rs.getInt("component_id");
            }
            if (compId == 0) return false;

            try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                deleteStmt.setInt(1, criterionId);
                boolean ok = deleteStmt.executeUpdate() > 0;
                if (ok) {
                    recalculateComponentMaxMarks(compId, conn);
                }
                return ok;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void recalculateComponentMaxMarks(int componentId, Connection conn) throws SQLException {
        String sumSql = "SELECT COALESCE(SUM(max_marks), 0) AS total FROM judging_criteria WHERE component_id = ?";
        String updateSql = "UPDATE judging_components SET max_marks = ? WHERE component_id = ?";
        int total = 0;
        try (PreparedStatement stmt = conn.prepareStatement(sumSql)) {
            stmt.setInt(1, componentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) total = rs.getInt("total");
            }
        }
        try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
            stmt.setInt(1, total);
            stmt.setInt(2, componentId);
            stmt.executeUpdate();
        }
    }

    public Map<Integer, Double> getTeamScoresForRound(String uniqueId, int roundId) {
        Map<Integer, Double> map = new HashMap<>();
        String sql = "SELECT s.criterion_id, s.score_awarded " +
                "FROM team_round_scores s " +
                "JOIN judging_criteria c ON s.criterion_id = c.criterion_id " +
                "JOIN judging_components comp ON c.component_id = comp.component_id " +
                "WHERE s.unique_id = ? AND comp.round_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, uniqueId);
            stmt.setInt(2, roundId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getInt("criterion_id"), rs.getDouble("score_awarded"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }

    public boolean saveTeamScores(String uniqueId, int roundId, int masterRoundId, Map<Integer, Double> scores, String evaluatorName) {
        String upsertSql = "INSERT INTO team_round_scores (unique_id, criterion_id, score_awarded, evaluator_name) " +
                "VALUES (?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE score_awarded = VALUES(score_awarded), evaluator_name = VALUES(evaluator_name)";
        double totalSum = 0;
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement stmt = conn.prepareStatement(upsertSql)) {
                for (Map.Entry<Integer, Double> entry : scores.entrySet()) {
                    stmt.setString(1, uniqueId);
                    stmt.setInt(2, entry.getKey());
                    stmt.setDouble(3, entry.getValue());
                    stmt.setString(4, evaluatorName != null ? evaluatorName : "Admin");
                    stmt.addBatch();
                    totalSum += entry.getValue();
                }
                stmt.executeBatch();
            }

            // Sync total sum into master `scores` table for leaderboard and round summary
            String scoreMasterSql = "INSERT INTO scores (unique_id, round_id, points) VALUES (?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE points = VALUES(points)";
            try (PreparedStatement stmt = conn.prepareStatement(scoreMasterSql)) {
                stmt.setString(1, uniqueId);
                stmt.setInt(2, masterRoundId);
                stmt.setDouble(3, totalSum);
                stmt.executeUpdate();
            }

            conn.commit();
            conn.setAutoCommit(true);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
