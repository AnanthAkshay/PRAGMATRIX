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
}
