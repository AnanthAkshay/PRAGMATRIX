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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

/**
 * Handles editing team profile / registration details (admin-only).
 * GET  /admin/edit-team?uniqueId=PMBZ001 → Displays pre-filled edit form
 * POST /admin/edit-team                  → Validates and persists updated team profile
 */
@WebServlet(name = "EditTeamServlet", urlPatterns = {"/admin/edit-team"})
public class EditTeamServlet extends HttpServlet {

    private final TeamDAO teamDAO = new TeamDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("adminId") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String uniqueId = req.getParameter("uniqueId");
        if (uniqueId == null || uniqueId.trim().isEmpty()) {
            uniqueId = req.getParameter("id");
        }

        if (uniqueId == null || uniqueId.trim().isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/admin/dashboard?error="
                    + URLEncoder.encode("Team identifier is required.", StandardCharsets.UTF_8));
            return;
        }

        try {
            Team team = teamDAO.findByUniqueId(uniqueId.trim());
            if (team == null) {
                resp.sendRedirect(req.getContextPath() + "/admin/dashboard?error="
                        + URLEncoder.encode("Team not found: " + uniqueId.trim(), StandardCharsets.UTF_8));
                return;
            }

            req.setAttribute("team", team);
            req.getRequestDispatcher("/WEB-INF/views/edit-team.jsp").forward(req, resp);

        } catch (SQLException e) {
            e.printStackTrace();
            resp.sendRedirect(req.getContextPath() + "/admin/dashboard?error="
                    + URLEncoder.encode("Database error loading team: " + e.getMessage(), StandardCharsets.UTF_8));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("adminId") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String uniqueId = trim(req.getParameter("uniqueId"));
        if (uniqueId == null || uniqueId.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/admin/dashboard?error="
                    + URLEncoder.encode("Missing team identifier.", StandardCharsets.UTF_8));
            return;
        }

        Team existingTeam;
        try {
            existingTeam = teamDAO.findByUniqueId(uniqueId);
            if (existingTeam == null) {
                resp.sendRedirect(req.getContextPath() + "/admin/dashboard?error="
                        + URLEncoder.encode("Team not found: " + uniqueId, StandardCharsets.UTF_8));
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            resp.sendRedirect(req.getContextPath() + "/admin/dashboard?error="
                    + URLEncoder.encode("Database error looking up team.", StandardCharsets.UTF_8));
            return;
        }

        String quizCode = existingTeam.getQuizCode();
        String collegeName = trim(req.getParameter("collegeName"));
        String teamLeadName = trim(req.getParameter("teamLeadName"));
        String leadEmail = trim(req.getParameter("leadEmail"));
        String member2Name = trimToNull(req.getParameter("member2Name"));
        String member3Name = trimToNull(req.getParameter("member3Name"));

        // Validation
        StringBuilder errors = new StringBuilder();
        if (collegeName == null || collegeName.isEmpty()) {
            errors.append("College name is required. ");
        }
        if (teamLeadName == null || teamLeadName.isEmpty()) {
            errors.append("Team lead name is required. ");
        }
        if (leadEmail == null || leadEmail.isEmpty()) {
            errors.append("Team lead email is required. ");
        } else if (!isValidEmail(leadEmail)) {
            errors.append("Please enter a valid email address. ");
        }

        if (errors.length() > 0) {
            // Keep user inputs for redisplay
            existingTeam.setCollegeName(collegeName);
            existingTeam.setTeamLeadName(teamLeadName);
            existingTeam.setLeadEmail(leadEmail);
            existingTeam.setMember2Name(member2Name);
            existingTeam.setMember3Name(member3Name);

            req.setAttribute("team", existingTeam);
            req.setAttribute("errorMessage", errors.toString().trim());
            req.getRequestDispatcher("/WEB-INF/views/edit-team.jsp").forward(req, resp);
            return;
        }

        try {
            existingTeam.setCollegeName(collegeName);
            existingTeam.setTeamLeadName(teamLeadName);
            existingTeam.setLeadEmail(leadEmail);
            existingTeam.setMember2Name(member2Name);
            existingTeam.setMember3Name(member3Name);

            boolean updated = teamDAO.updateTeamDetails(existingTeam);
            if (updated) {
                String successMsg = "Team " + uniqueId + " details updated successfully!";
                resp.sendRedirect(req.getContextPath() + "/admin/dashboard?quiz=" + quizCode
                        + "&success=" + URLEncoder.encode(successMsg, StandardCharsets.UTF_8));
            } else {
                req.setAttribute("team", existingTeam);
                req.setAttribute("errorMessage", "Could not update team details. Team may not exist.");
                req.getRequestDispatcher("/WEB-INF/views/edit-team.jsp").forward(req, resp);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            req.setAttribute("team", existingTeam);
            req.setAttribute("errorMessage", "Database update failed: " + e.getMessage());
            req.getRequestDispatcher("/WEB-INF/views/edit-team.jsp").forward(req, resp);
        }
    }

    private String trim(String s) {
        return (s == null) ? null : s.trim();
    }

    private String trimToNull(String s) {
        if (s == null) return null;
        String trimmed = s.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }
}
