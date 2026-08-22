package com.pragmatrix.servlet;

import com.pragmatrix.dao.TeamDAO;
import com.pragmatrix.model.Team;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Handles Team Dashboard login: entering the Team Code directly.
 * Locked server-side until August 24, 2026 at 9:00 AM IST (03:30 UTC).
 *
 * GET  /team-login → display the team login form or locked page
 * POST /team-login → validate Team Code, create session, redirect directly to dashboard
 */
@WebServlet(name = "TeamLoginServlet", urlPatterns = {"/team-login"})
public class TeamLoginServlet extends HttpServlet {

    // Set to true before event day to re-enable the Aug 24 9:00 AM IST lock
    private static final boolean LOCK_ENABLED = false;

    private static final Instant UNLOCK_TIME = ZonedDateTime.of(2026, 8, 24, 9, 0, 0, 0, ZoneId.of("Asia/Kolkata")).toInstant();

    private final TeamDAO teamDAO = new TeamDAO();

    private boolean isLocked() {
        return LOCK_ENABLED && Instant.now().isBefore(UNLOCK_TIME);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("teamUniqueId") != null) {
            resp.sendRedirect(req.getContextPath() + "/team/dashboard");
            return;
        }

        req.setAttribute("isLocked", isLocked());
        req.setAttribute("unlockTimeStr", "August 24, 2026 at 9:00 AM IST");
        req.getRequestDispatcher("/team-login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        // SERVER-SIDE TIME LOCK ENFORCEMENT
        if (isLocked()) {
            req.setAttribute("isLocked", true);
            req.setAttribute("error", "Team Login is currently locked. It will open on August 24, 2026 at 9:00 AM IST.");
            req.getRequestDispatcher("/team-login.jsp").forward(req, resp);
            return;
        }

        String teamCode = req.getParameter("teamCode");

        if (teamCode != null) teamCode = teamCode.trim().toUpperCase();

        // Validate input
        if (teamCode == null || teamCode.isEmpty()) {
            req.setAttribute("error", "Please enter your Team Code.");
            req.setAttribute("isLocked", false);
            req.getRequestDispatcher("/team-login.jsp").forward(req, resp);
            return;
        }

        try {
            // Look up team directly by unique ID / Team Code
            Team team = teamDAO.findByUniqueId(teamCode);
            if (team == null) {
                req.setAttribute("error", "Invalid Team Code. Please check and try again.");
                req.setAttribute("teamCode", teamCode);
                req.setAttribute("isLocked", false);
                req.getRequestDispatcher("/team-login.jsp").forward(req, resp);
                return;
            }

            // Create team session and redirect directly to team dashboard
            HttpSession session = req.getSession(true);
            session.setAttribute("teamUniqueId", team.getUniqueId());
            session.setMaxInactiveInterval(60 * 60); // 60 minutes session

            resp.sendRedirect(req.getContextPath() + "/team/dashboard");

        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("error", "An error occurred. Please try again. (" + e.getMessage() + ")");
            req.setAttribute("teamCode", teamCode);
            req.setAttribute("isLocked", false);
            req.getRequestDispatcher("/team-login.jsp").forward(req, resp);
        }
    }
}
