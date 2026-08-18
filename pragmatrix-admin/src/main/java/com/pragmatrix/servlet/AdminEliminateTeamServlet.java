package com.pragmatrix.servlet;

import com.pragmatrix.dao.TeamDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles BIZWIZX team elimination and restoration by administrators.
 * POST /admin/eliminate-teams
 */
@WebServlet(name = "AdminEliminateTeamServlet", urlPatterns = {"/admin/eliminate-teams"})
public class AdminEliminateTeamServlet extends HttpServlet {

    private final TeamDAO teamDAO = new TeamDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("adminId") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String quizCode = req.getParameter("quizCode");
        if (quizCode == null || quizCode.trim().isEmpty()) {
            quizCode = "BIZWIZX";
        }
        quizCode = quizCode.trim();

        String action = req.getParameter("action");
        boolean isEliminated = !"restore".equalsIgnoreCase(action);

        List<String> ids = new ArrayList<>();
        String[] teamIds = req.getParameterValues("teamIds");
        if (teamIds != null) {
            for (String id : teamIds) {
                if (id != null && !id.trim().isEmpty()) {
                    ids.add(id.trim());
                }
            }
        }

        String singleId = req.getParameter("uniqueId");
        if (singleId != null && !singleId.trim().isEmpty() && !ids.contains(singleId.trim())) {
            ids.add(singleId.trim());
        }

        try {
            if (!ids.isEmpty()) {
                teamDAO.updateEliminationStatusBatch(ids, isEliminated);
                String msg = isEliminated ? "Teams marked as eliminated successfully" : "Teams restored to active status successfully";
                resp.sendRedirect(req.getContextPath() + "/admin/dashboard?quiz=" + quizCode + "&success=" + msg.replace(" ", "+"));
            } else {
                resp.sendRedirect(req.getContextPath() + "/admin/dashboard?quiz=" + quizCode + "&error=No+teams+selected");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            resp.sendRedirect(req.getContextPath() + "/admin/dashboard?quiz=" + quizCode + "&error=Database+error+updating+teams:+ " + e.getMessage().replace(" ", "+"));
        }
    }
}
