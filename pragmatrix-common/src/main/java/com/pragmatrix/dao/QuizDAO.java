package com.pragmatrix.dao;

import com.pragmatrix.model.Quiz;
import com.pragmatrix.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for the quizzes table.
 */
public class QuizDAO {

    /**
     * Find a quiz by its code.
     */
    public Quiz findByCode(String quizCode) throws SQLException {
        String sql = "SELECT quiz_code, quiz_name, id_prefix FROM quizzes WHERE quiz_code = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, quizCode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    /**
     * Get all quizzes.
     */
    public List<Quiz> findAll() throws SQLException {
        List<Quiz> list = new ArrayList<>();
        String sql = "SELECT quiz_code, quiz_name, id_prefix FROM quizzes ORDER BY quiz_code";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    private Quiz mapRow(ResultSet rs) throws SQLException {
        Quiz q = new Quiz();
        q.setQuizCode(rs.getString("quiz_code"));
        q.setQuizName(rs.getString("quiz_name"));
        q.setIdPrefix(rs.getString("id_prefix"));
        return q;
    }
}
