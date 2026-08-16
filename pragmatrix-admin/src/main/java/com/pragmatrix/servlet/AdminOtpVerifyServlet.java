package com.pragmatrix.servlet;

import com.pragmatrix.dao.AdminDAO;
import com.pragmatrix.model.Admin;
import com.pragmatrix.util.EmailService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.security.SecureRandom;

/**
 * Handles admin OTP verification and resend requests.
 *
 * GET  /admin-otp-verify → display OTP verification form if pending email exists
 * POST /admin-otp-verify → verify OTP code or handle resend
 */
@WebServlet(name = "AdminOtpVerifyServlet", urlPatterns = {"/admin-otp-verify"})
public class AdminOtpVerifyServlet extends HttpServlet {

    private final AdminDAO adminDAO = new AdminDAO();
    private static final long OTP_EXPIRY_MS = 5 * 60 * 1000L; // 5 minutes
    private static final long RATE_LIMIT_MS = 30 * 1000L;     // 30 seconds
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("admin_pending_email") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        req.getRequestDispatcher("/admin-otp-verify.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("admin_pending_email") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String action = req.getParameter("action");
        String pendingEmail = (String) session.getAttribute("admin_pending_email");

        // Handle Resend OTP Request
        if ("resend".equalsIgnoreCase(action)) {
            Long lastSent = (Long) session.getAttribute("admin_otp_last_sent");
            long now = System.currentTimeMillis();
            if (lastSent != null && (now - lastSent < RATE_LIMIT_MS)) {
                long remainingSec = (RATE_LIMIT_MS - (now - lastSent)) / 1000L + 1;
                req.setAttribute("error", "Please wait " + remainingSec + " second(s) before requesting a new OTP.");
                req.getRequestDispatcher("/admin-otp-verify.jsp").forward(req, resp);
                return;
            }

            String newOtp = String.format("%06d", RANDOM.nextInt(1000000));
            long expiryTime = now + OTP_EXPIRY_MS;

            session.setAttribute("admin_otp_code", newOtp);
            session.setAttribute("admin_otp_expiry", expiryTime);
            session.setAttribute("admin_otp_last_sent", now);

            boolean sent = EmailService.sendAdminOtpEmail(pendingEmail, newOtp);
            if (!sent) {
                System.err.println("[ADMIN-OTP] Warning: Resend email failed via SMTP. Generated OTP: " + newOtp);
            }

            req.setAttribute("message", "A new OTP has been sent to " + pendingEmail + ". It will expire in 5 minutes.");
            req.getRequestDispatcher("/admin-otp-verify.jsp").forward(req, resp);
            return;
        }

        // Handle OTP Verification
        String enteredOtp = req.getParameter("otp");
        if (enteredOtp != null) enteredOtp = enteredOtp.trim();

        String expectedOtp = (String) session.getAttribute("admin_otp_code");
        Long expiryTime = (Long) session.getAttribute("admin_otp_expiry");
        long now = System.currentTimeMillis();

        if (enteredOtp == null || enteredOtp.isEmpty()) {
            req.setAttribute("error", "Please enter the 6-digit OTP.");
            req.getRequestDispatcher("/admin-otp-verify.jsp").forward(req, resp);
            return;
        }

        if (expiryTime == null || now > expiryTime) {
            req.setAttribute("error", "OTP has expired. Please request a new OTP.");
            req.getRequestDispatcher("/admin-otp-verify.jsp").forward(req, resp);
            return;
        }

        if (expectedOtp == null || !expectedOtp.equals(enteredOtp)) {
            req.setAttribute("error", "Invalid OTP code. Please check and try again.");
            req.getRequestDispatcher("/admin-otp-verify.jsp").forward(req, resp);
            return;
        }

        // OTP Verified successfully! Look up or create Admin session
        try {
            Admin admin = adminDAO.findByEmail(pendingEmail);
            int adminId = (admin != null) ? admin.getAdminId() : 1;
            String fullName = (admin != null && admin.getFullName() != null) ? admin.getFullName() : "Administrator";
            String username = (admin != null && admin.getUsername() != null) ? admin.getUsername() : pendingEmail;

            // Clear OTP session attributes
            session.removeAttribute("admin_pending_email");
            session.removeAttribute("admin_otp_code");
            session.removeAttribute("admin_otp_expiry");

            // Set Admin session credentials
            session.setAttribute("adminId", adminId);
            session.setAttribute("adminName", fullName);
            session.setAttribute("adminEmail", pendingEmail);
            session.setAttribute("adminUsername", username);
            session.setMaxInactiveInterval(60 * 60); // 60 minutes session

            resp.sendRedirect(req.getContextPath() + "/admin/dashboard");

        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("error", "An error occurred while creating admin session. Please try again.");
            req.getRequestDispatcher("/admin-otp-verify.jsp").forward(req, resp);
        }
    }
}
