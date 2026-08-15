package com.pragmatrix.listener;

import com.pragmatrix.dao.AdminDAO;
import com.pragmatrix.model.Admin;
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
