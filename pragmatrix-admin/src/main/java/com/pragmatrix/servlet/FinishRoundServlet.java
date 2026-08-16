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
 * Handles finishing or reopening a round.
 * POST /admin/finish-round
 */
@WebServlet(name = "FinishRoundServlet", urlPatterns = {"/admin/finish-round"})
public class FinishRoundServlet extends HttpServlet {

    private final RoundDAO roundDAO = new RoundDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            String roundIdStr = req.getParameter("roundId");
            String action = req.getParameter("action"); // "finish" or "reopen"
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

            String quiz = (quizCode != null && !quizCode.isEmpty()) ? quizCode : round.getQuizCode();

            if ("reopen".equals(action)) {
                roundDAO.reopenRound(roundId);
                resp.sendRedirect(req.getContextPath() + "/admin/dashboard?quiz=" + quiz + "&success=Round+reopened");
            } else {
                roundDAO.finishRound(roundId);
                resp.sendRedirect(req.getContextPath() + "/admin/dashboard?quiz=" + quiz + "&success=Round+finished");
            }

        } catch (Exception e) {
            e.printStackTrace();
            resp.sendRedirect(req.getContextPath() + "/admin/dashboard?error=Failed to update round status");
        }
    }
}
