package com.pragmatrix.listener;

import com.pragmatrix.dao.AdminDAO;
import com.pragmatrix.util.DBConnection;
import com.pragmatrix.util.PasswordUtil;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

/**
 * Application lifecycle listener.
 * Initialises the HikariCP connection pool on startup and seeds
 * default admin accounts if they don't exist.
 * Closes the pool on shutdown.
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
        System.out.println("[PRAGMATRIX] Shutting down connection pool...");
        DBConnection.close();
        System.out.println("[PRAGMATRIX] Connection pool closed.");
    }

    /**
     * Seed 2 admin accounts with bcrypt-hashed default passwords.
     * Primary: svs262003@gmail.com ("Admin 1")
     * Backup:  shirishvshandilya@gmail.com ("Admin 2 - Backup")
     */
    private void seedAdmins() throws Exception {
        AdminDAO dao = new AdminDAO();
        String hash = PasswordUtil.hashPassword(DEFAULT_PASSWORD);

        dao.insertIfNotExists("svs262003@gmail.com", hash, "Admin 1", "svs262003@gmail.com");
        dao.insertIfNotExists("shirishvshandilya@gmail.com", hash, "Admin 2 - Backup", "shirishvshandilya@gmail.com");
    }
}
