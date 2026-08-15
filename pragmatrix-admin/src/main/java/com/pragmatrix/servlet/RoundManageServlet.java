package com.pragmatrix.servlet;

import com.pragmatrix.dao.RoundDAO;
import com.pragmatrix.model.Round;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Handles updating round names (BIZWIZX only) and judging criteria.
 * POST /admin/round-manage
 */
@WebServlet(name = "RoundManageServlet", urlPatterns = {"/admin/round-manage"})
public class RoundManageServlet extends HttpServlet {

    private final RoundDAO roundDAO = new RoundDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        try {
            String roundIdStr = req.getParameter("roundId");
            String roundName = req.getParameter("roundName");
            String criteria = req.getParameter("judgingCriteria");
            String quizCode = req.getParameter("quizCode");

            if (roundIdStr == null || roundIdStr.isEmpty()) {
                resp.sendRedirect(req.getContextPath() + "/admin/dashboard");
                return;
            }

            int roundId = Integer.parseInt(roundIdStr);
            Round round = roundDAO.findById(roundId);

            if (round == null) {
                resp.sendRedirect(req.getContextPath() + "/admin/dashboard");
                return;
            }

            // Don't allow editing if round is finished
            if (round.isFinished()) {
                resp.sendRedirect(req.getContextPath() + "/admin/dashboard?quiz=" + quizCode + "&error=Round is finished");
                return;
            }

            if ("VORTEX".equals(round.getQuizCode())) {
                // VORTEX: only update criteria, names are fixed
                roundDAO.updateCriteria(roundId, criteria != null ? criteria.trim() : "");
            } else {
                // BIZWIZX: update both name and criteria
                roundDAO.updateRound(roundId,
                    (roundName != null && !roundName.trim().isEmpty()) ? roundName.trim() : round.getRoundName(),
                    criteria != null ? criteria.trim() : "");
            }

            resp.sendRedirect(req.getContextPath() + "/admin/dashboard?quiz=" + quizCode + "&success=Round updated");

        } catch (Exception e) {
            e.printStackTrace();
            resp.sendRedirect(req.getContextPath() + "/admin/dashboard?error=Failed to update round");
        }
    }
}
