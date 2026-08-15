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

/**
 * Handles Team Dashboard login: entering the Team Code directly.
 * GET  /team-login → display the team login form
 * POST /team-login → validate Team Code, create session, redirect directly to dashboard
 */
@WebServlet(name = "TeamLoginServlet", urlPatterns = {"/team-login"})
public class TeamLoginServlet extends HttpServlet {

    private final TeamDAO teamDAO = new TeamDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("teamUniqueId") != null) {
            resp.sendRedirect(req.getContextPath() + "/team/dashboard");
            return;
        }
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
            // Look up team directly by unique ID / Team Code
            Team team = teamDAO.findByUniqueId(teamCode);
            if (team == null) {
                req.setAttribute("error", "Invalid Team Code. Please check and try again.");
                req.setAttribute("teamCode", teamCode);
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
            req.getRequestDispatcher("/team-login.jsp").forward(req, resp);
        }
    }
}
