package com.pragmatrix.servlet;

import com.pragmatrix.dao.VortexCriteriaDAO;
import com.pragmatrix.model.VortexRound;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

/**
 * Servlet for managing VORTEX judging components and criteria.
 * Allows admin to view, add, edit, and delete components & criteria for any VORTEX round (including Round 4).
 */
@WebServlet(name = "ManageCriteriaServlet", urlPatterns = {"/admin/manage-criteria"})
public class ManageCriteriaServlet extends HttpServlet {

    private final VortexCriteriaDAO dao = new VortexCriteriaDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("adminId") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        List<VortexRound> rounds = dao.getAllRounds();
        req.setAttribute("vortexRounds", rounds);

        String roundIdParam = req.getParameter("roundId");
        int selectedRoundId = 1;
        if (roundIdParam != null && !roundIdParam.trim().isEmpty()) {
            try {
                selectedRoundId = Integer.parseInt(roundIdParam.trim());
            } catch (NumberFormatException ignored) {}
        }

        VortexRound currentRound = dao.getRoundById(selectedRoundId);
        if (currentRound == null && !rounds.isEmpty()) {
            currentRound = rounds.get(0);
        }

        req.setAttribute("currentRound", currentRound);
        req.setAttribute("selectedRoundId", selectedRoundId);
        req.getRequestDispatcher("/WEB-INF/views/manage-criteria.jsp").forward(req, resp);
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

        String action = req.getParameter("action");
        String roundIdStr = req.getParameter("roundId");
        int roundId = 1;
        try {
            if (roundIdStr != null) roundId = Integer.parseInt(roundIdStr.trim());
        } catch (NumberFormatException ignored) {}

        try {
            if ("addComponent".equalsIgnoreCase(action)) {
                String label = req.getParameter("componentLabel");
                String orderStr = req.getParameter("displayOrder");
                int order = 1;
                if (orderStr != null && !orderStr.trim().isEmpty()) {
                    order = Integer.parseInt(orderStr.trim());
                }
                dao.addComponent(roundId, label != null ? label.trim() : "", order);
            } else if ("editComponent".equalsIgnoreCase(action)) {
                int compId = Integer.parseInt(req.getParameter("componentId"));
                String label = req.getParameter("componentLabel");
                dao.updateComponent(compId, label != null ? label.trim() : "");
            } else if ("deleteComponent".equalsIgnoreCase(action)) {
                int compId = Integer.parseInt(req.getParameter("componentId"));
                dao.deleteComponent(compId);
            } else if ("addCriterion".equalsIgnoreCase(action)) {
                int compId = Integer.parseInt(req.getParameter("componentId"));
                String name = req.getParameter("criterionName");
                String lookFor = req.getParameter("judgesLookFor");
                int maxMarks = Integer.parseInt(req.getParameter("maxMarks"));
                String orderStr = req.getParameter("displayOrder");
                int order = 1;
                if (orderStr != null && !orderStr.trim().isEmpty()) {
                    order = Integer.parseInt(orderStr.trim());
                }
                dao.addCriterion(compId, name != null ? name.trim() : "", lookFor, maxMarks, order);
            } else if ("editCriterion".equalsIgnoreCase(action)) {
                int critId = Integer.parseInt(req.getParameter("criterionId"));
                String name = req.getParameter("criterionName");
                String lookFor = req.getParameter("judgesLookFor");
                int maxMarks = Integer.parseInt(req.getParameter("maxMarks"));
                dao.updateCriterion(critId, name != null ? name.trim() : "", lookFor, maxMarks);
            } else if ("deleteCriterion".equalsIgnoreCase(action)) {
                int critId = Integer.parseInt(req.getParameter("criterionId"));
                dao.deleteCriterion(critId);
            }
            resp.sendRedirect(req.getContextPath() + "/admin/manage-criteria?roundId=" + roundId + "&success=Criteria+updated+successfully");
        } catch (Exception e) {
            e.printStackTrace();
            resp.sendRedirect(req.getContextPath() + "/admin/manage-criteria?roundId=" + roundId + "&error=Error+updating+criteria");
        }
    }
}
