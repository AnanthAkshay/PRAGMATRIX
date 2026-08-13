package com.pragmatrix.servlet;

import com.pragmatrix.dao.TeamDAO;
import com.pragmatrix.dao.OtpDAO;
import com.pragmatrix.model.Team;
import com.pragmatrix.util.EmailService;
import com.pragmatrix.util.OtpUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.Timestamp;

/**
 * Handles the first step of team dashboard login: entering the Team Code.
 * GET  /team-login → display the team login form
 * POST /team-login → look up team, generate + email OTP, redirect to OTP verify
 */
@WebServlet(name = "TeamLoginServlet", urlPatterns = {"/team-login"})
public class TeamLoginServlet extends HttpServlet {

    private final TeamDAO teamDAO = new TeamDAO();
    private final OtpDAO otpDAO = new OtpDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.getRequestDispatcher("/team-login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        String teamCode = req.getParameter("teamCode");
        if (teamCode != null) teamCode = teamCode.trim().toUpperCase();

        // Validate input
        if (teamCode == null || teamCode.isEmpty()) {
            req.setAttribute("error", "Please enter your Team Code.");
            req.getRequestDispatcher("/team-login.jsp").forward(req, resp);
            return;
        }

        try {
            // Look up team
            Team team = teamDAO.findByUniqueId(teamCode);
            if (team == null) {
                req.setAttribute("error", "Team code not found \u2014 please check and try again.");
                req.setAttribute("teamCode", teamCode);
                req.getRequestDispatcher("/team-login.jsp").forward(req, resp);
                return;
            }

            // Rate-limit: max 5 OTP requests per team per 15 minutes
            int recentCount = otpDAO.countRecentOtps(teamCode, 15);
            if (recentCount >= 5) {
                req.setAttribute("error", "Too many OTP requests. Please wait a few minutes before trying again.");
                req.setAttribute("teamCode", teamCode);
                req.getRequestDispatcher("/team-login.jsp").forward(req, resp);
                return;
            }

            // Invalidate any existing unused OTPs for this team
            otpDAO.invalidateAllForTeam(teamCode);

            // Generate OTP (6-digit, cryptographically secure)
            String otpCode = OtpUtil.generateOtp();

            // Set expiry: 5 minutes from now
            Timestamp expiresAt = new Timestamp(System.currentTimeMillis() + (5 * 60 * 1000));

            // Store OTP in database
            otpDAO.insertOtp(teamCode, otpCode, expiresAt);

            // Email OTP to team lead
            boolean emailSent = EmailService.sendOtpEmail(team.getLeadEmail(), otpCode);

            // Store pending team info in session for the OTP verify step
            HttpSession session = req.getSession(true);
            session.setAttribute("pendingTeamId", teamCode);
            session.setAttribute("pendingTeamEmail", team.getLeadEmail());
            session.setAttribute("otpLastSentAt", System.currentTimeMillis());

            if (!emailSent) {
                System.err.println("[PRAGMATRIX] OTP email failed for team " + teamCode + " — OTP is still stored in DB.");
            }

            resp.sendRedirect(req.getContextPath() + "/team-otp-verify");

        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("error", "An error occurred. Please try again. (" + e.getMessage() + ")");
            req.setAttribute("teamCode", teamCode);
            req.getRequestDispatcher("/team-login.jsp").forward(req, resp);
        }
    }
}
