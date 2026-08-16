package com.pragmatrix.servlet;

import com.pragmatrix.dao.TeamDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Servlet for removing a team and cascading deletion of all associated data.
 * Guarded by admin session check.
 */
@WebServlet(name = "AdminDeleteTeamServlet", urlPatterns = {"/admin/delete-team"})
public class AdminDeleteTeamServlet extends HttpServlet {

    private final TeamDAO teamDAO = new TeamDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("adminId") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String uniqueId = req.getParameter("uniqueId");
        String quizCode = req.getParameter("quizCode");
        if (quizCode == null || quizCode.trim().isEmpty()) {
            quizCode = "BIZWIZX";
        }

        if (uniqueId == null || uniqueId.trim().isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/admin/dashboard?quiz=" + quizCode + "&error=Invalid+team+ID");
            return;
        }

        try {
            boolean deleted = teamDAO.deleteByUniqueId(uniqueId.trim());
            if (deleted) {
                resp.sendRedirect(req.getContextPath() + "/admin/dashboard?quiz=" + quizCode + "&success=" + URLEncoder.encode("Team " + uniqueId + " removed successfully", StandardCharsets.UTF_8));
            } else {
                resp.sendRedirect(req.getContextPath() + "/admin/dashboard?quiz=" + quizCode + "&error=" + URLEncoder.encode("Team not found or already deleted", StandardCharsets.UTF_8));
            }
        } catch (Exception e) {
            e.printStackTrace();
            resp.sendRedirect(req.getContextPath() + "/admin/dashboard?quiz=" + quizCode + "&error=" + URLEncoder.encode("Failed to remove team: " + e.getMessage(), StandardCharsets.UTF_8));
        }
    }
}
