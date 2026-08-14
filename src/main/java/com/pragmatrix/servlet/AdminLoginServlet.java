package com.pragmatrix.servlet;

import com.pragmatrix.dao.AdminDAO;
import com.pragmatrix.model.Admin;
import com.pragmatrix.util.PasswordUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles admin authentication via email + hashed password (no OTP).
 * Features rate-limiting to prevent brute force attacks.
 *
 * GET  /login → display login form
 * POST /login → authenticate admin, create session, redirect to dashboard
 */
@WebServlet(name = "AdminLoginServlet", urlPatterns = {"/login"})
public class AdminLoginServlet extends HttpServlet {

    private final AdminDAO adminDAO = new AdminDAO();

    // Rate-limiting configuration: max 5 failed attempts = 5 min lockout
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final long LOCKOUT_DURATION_MS = 5 * 60 * 1000L; // 5 minutes
    private static final long ATTEMPT_WINDOW_MS = 15 * 60 * 1000L;   // 15 minutes

    private static class AttemptRecord {
        int failedCount = 0;
        long firstAttemptTime = System.currentTimeMillis();
        long lockedUntil = 0;
    }

    private static final ConcurrentHashMap<String, AttemptRecord> ATTEMPT_CACHE = new ConcurrentHashMap<>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // If already authenticated, redirect directly to admin dashboard
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("adminId") != null) {
            resp.sendRedirect(req.getContextPath() + "/admin/dashboard");
            return;
        }
        req.getRequestDispatcher("/admin-login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        String email = req.getParameter("email");
        String password = req.getParameter("password");

        if (email != null) email = email.trim();

        // Validate basic inputs
        if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
            req.setAttribute("error", "Email and password are required.");
            req.setAttribute("email", email);
            req.getRequestDispatcher("/admin-login.jsp").forward(req, resp);
            return;
        }

        String emailKey = email.toLowerCase();
        long now = System.currentTimeMillis();

        // Check if account / IP is currently locked out
        AttemptRecord record = ATTEMPT_CACHE.get(emailKey);
        if (record != null && now < record.lockedUntil) {
            long remainingMinutes = Math.max(1, (record.lockedUntil - now + 59999L) / 60000L);
            req.setAttribute("error", "Too many failed login attempts. Account temporarily locked. Please try again in " + remainingMinutes + " minute(s).");
            req.setAttribute("email", email);
            req.getRequestDispatcher("/admin-login.jsp").forward(req, resp);
            return;
        }

        try {
            Admin admin = adminDAO.findByEmail(emailKey);

            // Generic error on failure to not reveal whether email exists
            if (admin == null || !PasswordUtil.checkPassword(password, admin.getPasswordHash())) {
                registerFailedAttempt(emailKey, now);

                req.setAttribute("error", "Invalid email or password.");
                req.setAttribute("email", email);
                req.getRequestDispatcher("/admin-login.jsp").forward(req, resp);
                return;
            }

            // Authentication successful: clear failed attempts
            ATTEMPT_CACHE.remove(emailKey);

            // Create admin session
            HttpSession session = req.getSession(true);
            session.setAttribute("adminId", admin.getAdminId());
            session.setAttribute("adminName", admin.getFullName());
            session.setAttribute("adminEmail", admin.getEmail());
            session.setAttribute("adminUsername", admin.getUsername());
            session.setMaxInactiveInterval(60 * 60); // 60 minutes session

            resp.sendRedirect(req.getContextPath() + "/admin/dashboard");

        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("error", "An unexpected error occurred during login. Please try again.");
            req.setAttribute("email", email);
            req.getRequestDispatcher("/admin-login.jsp").forward(req, resp);
        }
    }

    private void registerFailedAttempt(String emailKey, long now) {
        ATTEMPT_CACHE.compute(emailKey, (k, v) -> {
            if (v == null || (now - v.firstAttemptTime > ATTEMPT_WINDOW_MS && now > v.lockedUntil)) {
                v = new AttemptRecord();
            }
            v.failedCount++;
            if (v.failedCount >= MAX_FAILED_ATTEMPTS) {
                v.lockedUntil = now + LOCKOUT_DURATION_MS;
            }
            return v;
        });
    }
}
