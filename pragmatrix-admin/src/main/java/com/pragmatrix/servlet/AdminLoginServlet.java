package com.pragmatrix.servlet;

import com.pragmatrix.util.EmailService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;

/**
 * Handles admin authentication step 1 (Email input -> OTP generation).
 * Restricted strictly to 2 authorized email addresses:
 * 1. svs262003@gmail.com
 * 2. ananthakshay2006@gmail.com
 *
 * GET  /login -> display admin email login form
 * POST /login -> validate email, generate 6-digit OTP, send email, forward to OTP verification
 */
@WebServlet(name = "AdminLoginServlet", urlPatterns = {"/login"})
public class AdminLoginServlet extends HttpServlet {

    private static final List<String> AUTHORIZED_ADMIN_EMAILS = Arrays.asList(
            "svs262003@gmail.com",
            "ananthakshay2006@gmail.com"
    );

    private static final long OTP_EXPIRY_MS = 5 * 60 * 1000L; // 5 minutes
    private static final long RATE_LIMIT_MS = 30 * 1000L;     // 30 seconds between resends
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
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

        String emailInput = req.getParameter("email");
        if (emailInput == null || emailInput.trim().isEmpty()) {
            emailInput = req.getParameter("username");
        }

        if (emailInput == null || emailInput.trim().isEmpty()) {
            req.setAttribute("error", "Please enter your email address.");
            req.getRequestDispatcher("/admin-login.jsp").forward(req, resp);
            return;
        }

        String email = emailInput.trim().toLowerCase();

        // 1. Strict 2-email restriction check
        if (!AUTHORIZED_ADMIN_EMAILS.contains(email)) {
            req.setAttribute("error", "This email is not authorized for admin access.");
            req.setAttribute("email", emailInput);
            req.getRequestDispatcher("/admin-login.jsp").forward(req, resp);
            return;
        }

        HttpSession session = req.getSession(true);

        // 2. Rate limiting check
        Long lastSent = (Long) session.getAttribute("admin_otp_last_sent");
        long now = System.currentTimeMillis();
        if (lastSent != null && (now - lastSent < RATE_LIMIT_MS)) {
            long remainingSec = (RATE_LIMIT_MS - (now - lastSent)) / 1000L + 1;
            req.setAttribute("error", "Please wait " + remainingSec + " second(s) before requesting a new OTP.");
            req.setAttribute("email", emailInput);
            req.getRequestDispatcher("/admin-login.jsp").forward(req, resp);
            return;
        }

        // 3. Generate 6-digit numeric OTP
        String otpCode = String.format("%06d", RANDOM.nextInt(1000000));
        long expiryTime = now + OTP_EXPIRY_MS;

        session.setAttribute("admin_pending_email", email);
        session.setAttribute("admin_otp_code", otpCode);
        session.setAttribute("admin_otp_expiry", expiryTime);
        session.setAttribute("admin_otp_last_sent", now);

        // 4. Send OTP email via SMTP
        boolean sent = EmailService.sendAdminOtpEmail(email, otpCode);
        if (!sent) {
            System.err.println("[ADMIN-LOGIN] Warning: Email failed to send via SMTP. Generated OTP: " + otpCode);
        }

        req.setAttribute("message", "A 6-digit OTP has been sent to " + email + ". It will expire in 5 minutes.");
        req.getRequestDispatcher("/admin-otp-verify.jsp").forward(req, resp);
    }

    public static List<String> getAuthorizedAdminEmails() {
        return AUTHORIZED_ADMIN_EMAILS;
    }
}
