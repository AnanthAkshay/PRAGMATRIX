package com.pragmatrix.servlet;

import com.pragmatrix.dao.OtpDAO;
import com.pragmatrix.dao.TeamSessionDAO;
import com.pragmatrix.model.TeamLoginOtp;
import com.pragmatrix.util.EmailService;
import com.pragmatrix.util.OtpUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.util.Base64;

/**
 * Handles the second step of team dashboard login: OTP verification.
 * GET  /team-otp-verify → display OTP entry form (with masked email)
 * POST /team-otp-verify → verify OTP, create session, redirect to dashboard
 * POST /team-otp-verify?action=resend → resend OTP (rate-limited)
 */
@WebServlet(name = "TeamOtpVerifyServlet", urlPatterns = {"/team-otp-verify"})
public class TeamOtpVerifyServlet extends HttpServlet {

    private final OtpDAO otpDAO = new OtpDAO();
    private final TeamSessionDAO sessionDAO = new TeamSessionDAO();
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("pendingTeamId") == null) {
            resp.sendRedirect(req.getContextPath() + "/team-login");
            return;
        }

        String email = (String) session.getAttribute("pendingTeamEmail");
        req.setAttribute("maskedEmail", OtpUtil.maskEmail(email));
        req.setAttribute("teamCode", session.getAttribute("pendingTeamId"));

        req.getRequestDispatcher("/team-otp-verify.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("pendingTeamId") == null) {
            resp.sendRedirect(req.getContextPath() + "/team-login");
            return;
        }

        String teamCode = (String) session.getAttribute("pendingTeamId");
        String email = (String) session.getAttribute("pendingTeamEmail");
        String action = req.getParameter("action");

        // --- Handle resend OTP ---
        if ("resend".equals(action)) {
            handleResend(req, resp, session, teamCode, email);
            return;
        }

        // --- Handle OTP verification ---
        String enteredOtp = req.getParameter("otp");
        if (enteredOtp != null) enteredOtp = enteredOtp.trim();

        if (enteredOtp == null || enteredOtp.isEmpty()) {
            req.setAttribute("error", "Please enter the OTP.");
            req.setAttribute("maskedEmail", OtpUtil.maskEmail(email));
            req.setAttribute("teamCode", teamCode);
            req.getRequestDispatcher("/team-otp-verify.jsp").forward(req, resp);
            return;
        }

        try {
            TeamLoginOtp otp = otpDAO.findLatestUnusedOtp(teamCode);

            if (otp == null) {
                req.setAttribute("error", "No valid OTP found. Please request a new one.");
                req.setAttribute("maskedEmail", OtpUtil.maskEmail(email));
                req.setAttribute("teamCode", teamCode);
                req.getRequestDispatcher("/team-otp-verify.jsp").forward(req, resp);
                return;
            }

            // Check attempt count (max 5 wrong tries per OTP)
            if (otp.getAttemptCount() >= 5) {
                otpDAO.markUsed(otp.getOtpId()); // Lock this OTP
                req.setAttribute("error", "Too many incorrect attempts. Please request a new OTP.");
                req.setAttribute("maskedEmail", OtpUtil.maskEmail(email));
                req.setAttribute("teamCode", teamCode);
                req.getRequestDispatcher("/team-otp-verify.jsp").forward(req, resp);
                return;
            }

            // Verify OTP
            if (!otp.getOtpCode().equals(enteredOtp)) {
                otpDAO.incrementAttemptCount(otp.getOtpId());
                int remaining = 4 - otp.getAttemptCount(); // already incremented once
                req.setAttribute("error", "Incorrect OTP. " + Math.max(remaining, 0) + " attempt(s) remaining.");
                req.setAttribute("maskedEmail", OtpUtil.maskEmail(email));
                req.setAttribute("teamCode", teamCode);
                req.getRequestDispatcher("/team-otp-verify.jsp").forward(req, resp);
                return;
            }

            // OTP is valid — mark as used
            otpDAO.markUsed(otp.getOtpId());

            // Create a team session
            String teamSessionId = generateSecureToken();
            Timestamp sessionExpiry = new Timestamp(System.currentTimeMillis() + (30 * 60 * 1000)); // 30 min

            // Clean old sessions for this team
            sessionDAO.invalidateAllForTeam(teamCode);
            sessionDAO.insertSession(teamSessionId, teamCode, sessionExpiry);

            // Set session attributes for the team dashboard
            session.removeAttribute("pendingTeamId");
            session.removeAttribute("pendingTeamEmail");
            session.removeAttribute("otpLastSentAt");
            session.setAttribute("teamUniqueId", teamCode);
            session.setAttribute("teamSessionId", teamSessionId);

            resp.sendRedirect(req.getContextPath() + "/team/dashboard");

        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("error", "An error occurred. Please try again.");
            req.setAttribute("maskedEmail", OtpUtil.maskEmail(email));
            req.setAttribute("teamCode", teamCode);
            req.getRequestDispatcher("/team-otp-verify.jsp").forward(req, resp);
        }
    }

    private void handleResend(HttpServletRequest req, HttpServletResponse resp,
                              HttpSession session, String teamCode, String email)
            throws ServletException, IOException {
        try {
            // Rate-limit: 1 resend per 60 seconds
            Long lastSentAt = (Long) session.getAttribute("otpLastSentAt");
            if (lastSentAt != null && (System.currentTimeMillis() - lastSentAt) < 60000) {
                req.setAttribute("error", "Please wait before requesting another OTP.");
                req.setAttribute("maskedEmail", OtpUtil.maskEmail(email));
                req.setAttribute("teamCode", teamCode);
                req.getRequestDispatcher("/team-otp-verify.jsp").forward(req, resp);
                return;
            }

            // Rate-limit: max 5 OTPs per 15 minutes
            int recentCount = otpDAO.countRecentOtps(teamCode, 15);
            if (recentCount >= 5) {
                req.setAttribute("error", "Too many OTP requests. Please wait a few minutes.");
                req.setAttribute("maskedEmail", OtpUtil.maskEmail(email));
                req.setAttribute("teamCode", teamCode);
                req.getRequestDispatcher("/team-otp-verify.jsp").forward(req, resp);
                return;
            }

            // Invalidate old OTPs
            otpDAO.invalidateAllForTeam(teamCode);

            // Generate and store new OTP
            String otpCode = OtpUtil.generateOtp();
            Timestamp expiresAt = new Timestamp(System.currentTimeMillis() + (5 * 60 * 1000));
            otpDAO.insertOtp(teamCode, otpCode, expiresAt);

            // Send email
            EmailService.sendOtpEmail(email, otpCode);
            session.setAttribute("otpLastSentAt", System.currentTimeMillis());

            req.setAttribute("success", "A new OTP has been sent to your email.");
            req.setAttribute("maskedEmail", OtpUtil.maskEmail(email));
            req.setAttribute("teamCode", teamCode);
            req.getRequestDispatcher("/team-otp-verify.jsp").forward(req, resp);

        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("error", "Failed to resend OTP. Please try again.");
            req.setAttribute("maskedEmail", OtpUtil.maskEmail(email));
            req.setAttribute("teamCode", teamCode);
            req.getRequestDispatcher("/team-otp-verify.jsp").forward(req, resp);
        }
    }

    /**
     * Generate a cryptographically secure random token for session tracking.
     */
    private String generateSecureToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
