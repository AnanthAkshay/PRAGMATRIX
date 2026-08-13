package com.pragmatrix.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Transaction-safe unique ID generator for teams.
 * <p>
 * Generates IDs like PMBZ001, PMBZ002, ..., PMVX001, PMVX002, ...
 * Uses SELECT ... FOR UPDATE within a transaction to prevent
 * duplicate IDs under concurrent registration.
 * </p>
 */
public class IdGenerator {

    private IdGenerator() {} // utility class

    /**
     * Generate the next unique ID for a given quiz prefix.
     * <p>
     * MUST be called within an active transaction on the provided connection
     * (autoCommit must be false). The caller is responsible for committing
     * or rolling back the transaction.
     * </p>
     *
     * @param conn      a JDBC connection with autoCommit=false
     * @param idPrefix  the prefix (e.g. "PMBZ" or "PMVX")
     * @return the next unique ID (e.g. "PMBZ001")
     * @throws SQLException if a database error occurs
     */
    public static String generateNextId(Connection conn, String idPrefix) throws SQLException {
        // Find the maximum existing numeric suffix for this prefix.
        // We lock the relevant rows to prevent race conditions.
        String sql = "SELECT unique_id FROM teams WHERE unique_id LIKE ? ORDER BY unique_id DESC LIMIT 1 FOR UPDATE";

        int nextNum = 1;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, idPrefix + "%");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String lastId = rs.getString("unique_id");
                    // Extract the numeric part after the prefix
                    String numPart = lastId.substring(idPrefix.length());
                    nextNum = Integer.parseInt(numPart) + 1;
                }
            }
        }

        // Zero-pad: 3 digits by default, extend to 4+ if needed
        int padWidth = (nextNum > 999) ? 4 : 3;
        return idPrefix + String.format("%0" + padWidth + "d", nextNum);
    }
}
