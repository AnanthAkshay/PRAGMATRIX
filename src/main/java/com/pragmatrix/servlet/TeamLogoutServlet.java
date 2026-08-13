package com.pragmatrix.servlet;

import com.pragmatrix.dao.TeamSessionDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Logs a team out of the Team Dashboard.
 * Invalidates both the server-side team_sessions row and the HttpSession attributes.
 *
 * GET /team-logout → invalidate session, redirect to /team-login
 */
@WebServlet(name = "TeamLogoutServlet", urlPatterns = {"/team-logout"})
public class TeamLogoutServlet extends HttpServlet {

    private final TeamSessionDAO sessionDAO = new TeamSessionDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session != null) {
            // Invalidate the DB session row
            String teamSessionId = (String) session.getAttribute("teamSessionId");
            if (teamSessionId != null) {
                try {
                    sessionDAO.invalidateSession(teamSessionId);
                } catch (Exception e) {
                    System.err.println("[PRAGMATRIX] Error invalidating team session: " + e.getMessage());
                }
            }

            // Clear team-related session attributes
            session.removeAttribute("teamUniqueId");
            session.removeAttribute("teamSessionId");
            session.removeAttribute("pendingTeamId");
            session.removeAttribute("pendingTeamEmail");
            session.removeAttribute("otpLastSentAt");
        }

        resp.sendRedirect(req.getContextPath() + "/team-login");
    }
}
