package com.pragmatrix.listener;

import com.pragmatrix.dao.AdminDAO;
import com.pragmatrix.model.Admin;
import com.pragmatrix.util.DBConnection;
import com.pragmatrix.util.PasswordUtil;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

/**
 * Application lifecycle listener.
 * Initialises the HikariCP connection pool on startup and seeds
 * default admin accounts if they don't exist.
 * Closes the pool, deregisters JDBC drivers, and stops MySQL cleanup threads on shutdown.
 */
@WebListener
public class AppContextListener implements ServletContextListener {

    private static final String DEFAULT_PASSWORD = "Pragmatrix@2026";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("[PRAGMATRIX] Initialising database connection pool...");
        DBConnection.init();
        System.out.println("[PRAGMATRIX] Connection pool ready.");

        // Seed admin accounts if they don't exist
        try {
            seedAdmins();
            System.out.println("[PRAGMATRIX] Admin accounts verified/seeded (Default initial password: " + DEFAULT_PASSWORD + ").");
        } catch (Exception e) {
            System.err.println("[PRAGMATRIX] Warning: Could not seed admin accounts: " + e.getMessage());
            e.printStackTrace();
        }

        // Verify/create leaderboard view if supported by database permissions
        try {
            ensureLeaderboardView();
        } catch (Exception e) {
            System.err.println("[PRAGMATRIX] Note: Leaderboard view check: " + e.getMessage());
        }

        // Ensure VORTEX Round 4 is named GRAND FINALE across tables
        try {
            ensureVortexRoundNames();
        } catch (Exception e) {
            System.err.println("[PRAGMATRIX] Note: VORTEX Round 4 name check: " + e.getMessage());
        }

        // Ensure is_eliminated and advanced_to_finale columns exist on teams table
        try {
            ensureTeamColumns();
        } catch (Exception e) {
            System.err.println("[PRAGMATRIX] Note: Team columns check: " + e.getMessage());
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Step 1: Shut down HikariCP connection pool first
        System.out.println("[PRAGMATRIX] Shutting down connection pool...");
        try {
            DBConnection.close();
            System.out.println("[PRAGMATRIX] Connection pool closed.");
        } catch (Throwable t) {
            System.err.println("[PRAGMATRIX] Error closing connection pool: " + t.getMessage());
        }

        // Step 2: Deregister JDBC drivers registered by this webapp ClassLoader
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            if (driver.getClass().getClassLoader() == cl) {
                try {
                    DriverManager.deregisterDriver(driver);
                    System.out.println("[PRAGMATRIX] Deregistered JDBC driver: " + driver);
                } catch (SQLException e) {
                    System.err.println("[PRAGMATRIX] Error deregistering JDBC driver " + driver + ": " + e.getMessage());
                } catch (Throwable t) {
                    System.err.println("[PRAGMATRIX] Unexpected error deregistering JDBC driver " + driver + ": " + t.getMessage());
                }
            }
        }

        // Step 3: Shut down MySQL AbandonedConnectionCleanupThread
        try {
            com.mysql.cj.jdbc.AbandonedConnectionCleanupThread.checkedShutdown();
            System.out.println("[PRAGMATRIX] MySQL AbandonedConnectionCleanupThread successfully stopped.");
        } catch (Throwable t) {
            System.err.println("[PRAGMATRIX] Warning: Could not shutdown MySQL AbandonedConnectionCleanupThread: " + t.getMessage());
        }
    }

    /**
     * Seed 10 admin accounts (admin1 to admin10) with bcrypt-hashed default password Pragmatrix@2026.
     */
    private void seedAdmins() throws Exception {
        AdminDAO dao = new AdminDAO();
        String hash = PasswordUtil.hashPassword(DEFAULT_PASSWORD);

        String[] numberWords = {"One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten"};
        for (int i = 1; i <= 10; i++) {
            String username = "admin" + i;
            String fullName = "Admin " + numberWords[i - 1];
            String email = "admin" + i + "@pragmatrix.com";
            seedOrUpdate(dao, username, hash, fullName, email);
        }
    }

    private void seedOrUpdate(AdminDAO dao, String username, String hash, String fullName, String email) throws Exception {
        Admin existing = dao.findByUsernameOrEmail(username);
        if (existing == null) {
            dao.insertIfNotExists(username, hash, fullName, email);
        } else if (!PasswordUtil.checkPassword(DEFAULT_PASSWORD, existing.getPasswordHash())) {
            dao.updatePassword(existing.getAdminId(), hash);
            System.out.println("[PRAGMATRIX] Updated password hash for admin: " + username);
        }
    }

    /**
     * Creates or replaces the computed leaderboard view in the database if possible.
     */
    private void ensureLeaderboardView() {
        String sql = "CREATE OR REPLACE VIEW leaderboard AS "
                   + "SELECT t.unique_id, t.college_name, t.team_lead_name, t.quiz_code, "
                   + "COALESCE(SUM(CASE WHEN r.is_finished = TRUE THEN s.points ELSE 0 END), 0) AS total_points "
                   + "FROM teams t "
                   + "LEFT JOIN scores s ON t.unique_id = s.unique_id "
                   + "LEFT JOIN rounds r ON s.round_id = r.round_id "
                   + "GROUP BY t.unique_id, t.college_name, t.team_lead_name, t.quiz_code "
                   + "ORDER BY total_points DESC";
        try (java.sql.Connection conn = DBConnection.getConnection();
             java.sql.Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
            System.out.println("[PRAGMATRIX] Leaderboard view verified/created.");
        } catch (Exception e) {
            System.err.println("[PRAGMATRIX] Note: Could not create leaderboard view (using direct queries): " + e.getMessage());
        }
    }

    /**
     * Ensures VORTEX rounds follow the order: KAIROS (1) -> TREORAI (2) -> ENMA (3) -> GRAND FINALE (4)
     * in both vortex_rounds and rounds tables.
     */
    private void ensureVortexRoundNames() {
        try (java.sql.Connection conn = DBConnection.getConnection();
             java.sql.Statement stmt = conn.createStatement()) {
            // Update vortex_rounds display_order and names
            stmt.executeUpdate("UPDATE vortex_rounds SET display_order = 1 WHERE round_name = 'KAIROS'");
            stmt.executeUpdate("UPDATE vortex_rounds SET round_name = 'TREORAI', display_order = 2 WHERE round_name = 'TREORAI' OR round_name = 'THEORAI'");
            stmt.executeUpdate("UPDATE vortex_rounds SET display_order = 3 WHERE round_name = 'ENMA'");
            stmt.executeUpdate("UPDATE vortex_rounds SET round_name = 'GRAND FINALE', display_order = 4 WHERE round_name = 'GRAND FINALE' OR round_name = 'Round 4' OR round_name = 'SLANCIO' OR display_order = 4");

            // Update master rounds table
            stmt.executeUpdate("UPDATE rounds SET round_name = 'KAIROS' WHERE quiz_code = 'VORTEX' AND round_number = 1");
            stmt.executeUpdate("UPDATE rounds SET round_name = 'TREORAI' WHERE quiz_code = 'VORTEX' AND round_number = 2");
            stmt.executeUpdate("UPDATE rounds SET round_name = 'ENMA' WHERE quiz_code = 'VORTEX' AND round_number = 3");
            stmt.executeUpdate("UPDATE rounds SET round_name = 'GRAND FINALE' WHERE quiz_code = 'VORTEX' AND round_number = 4");

            System.out.println("[PRAGMATRIX] VORTEX rounds verified/updated to: KAIROS (1) -> TREORAI (2) -> ENMA (3) -> GRAND FINALE (4).");
        } catch (Exception e) {
            System.err.println("[PRAGMATRIX] Note: Could not update VORTEX round names/order: " + e.getMessage());
        }
    }

    /**
     * Ensures is_eliminated and advanced_to_finale columns exist on teams table.
     */
    private void ensureTeamColumns() {
        try (java.sql.Connection conn = DBConnection.getConnection();
             java.sql.Statement stmt = conn.createStatement()) {
            try {
                stmt.executeUpdate("ALTER TABLE teams ADD COLUMN is_eliminated BOOLEAN NOT NULL DEFAULT FALSE");
                System.out.println("[PRAGMATRIX] Added is_eliminated column to teams table.");
            } catch (SQLException ignored) {
                // Column already exists
            }
            try {
                stmt.executeUpdate("ALTER TABLE teams ADD COLUMN advanced_to_finale BOOLEAN NOT NULL DEFAULT FALSE");
                System.out.println("[PRAGMATRIX] Added advanced_to_finale column to teams table.");
            } catch (SQLException ignored) {
                // Column already exists
            }
        } catch (Exception e) {
            System.err.println("[PRAGMATRIX] Note: Could not check/add team columns: " + e.getMessage());
        }
    }
}
