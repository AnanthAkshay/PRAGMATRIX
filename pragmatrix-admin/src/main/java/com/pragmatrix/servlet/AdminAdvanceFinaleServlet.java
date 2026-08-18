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
 * Handles promoting/advancing teams to VORTEX GRAND FINALE.
 * POST /admin/advance-finale
 */
@WebServlet(name = "AdminAdvanceFinaleServlet", urlPatterns = {"/admin/advance-finale"})
public class AdminAdvanceFinaleServlet extends HttpServlet {

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
            quizCode = "VORTEX";
        }
        quizCode = quizCode.trim().toUpperCase();

        List<String> selectedIds = new ArrayList<>();
        String[] teamIds = req.getParameterValues("teamIds");
        if (teamIds != null) {
            for (String id : teamIds) {
                if (id != null && !id.trim().isEmpty()) {
                    selectedIds.add(id.trim());
                }
            }
        }

        try {
            teamDAO.setGrandFinaleAdvancement(quizCode, selectedIds);
            String successMsg = selectedIds.size() + " team(s) confirmed for GRAND FINALE advancement";
            resp.sendRedirect(req.getContextPath() + "/admin/dashboard?quiz=" + quizCode + "&success=" + successMsg.replace(" ", "+"));
        } catch (SQLException e) {
            e.printStackTrace();
            resp.sendRedirect(req.getContextPath() + "/admin/dashboard?quiz=" + quizCode + "&error=Database+error+updating+advancement:+ " + e.getMessage().replace(" ", "+"));
        }
    }
}
