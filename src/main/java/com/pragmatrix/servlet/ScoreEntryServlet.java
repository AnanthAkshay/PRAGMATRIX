package com.pragmatrix.servlet;

import com.pragmatrix.dao.RoundDAO;
import com.pragmatrix.dao.ScoreDAO;
import com.pragmatrix.dao.TeamDAO;
import com.pragmatrix.model.Round;
import com.pragmatrix.model.Score;
import com.pragmatrix.model.Team;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Score entry screen for a specific round.
 * GET  /admin/score-entry?roundId=X → display score entry table
 * POST /admin/score-entry           → save all scores for the round
 */
@WebServlet(name = "ScoreEntryServlet", urlPatterns = {"/admin/score-entry"})
public class ScoreEntryServlet extends HttpServlet {

    private final RoundDAO roundDAO = new RoundDAO();
    private final TeamDAO teamDAO = new TeamDAO();
    private final ScoreDAO scoreDAO = new ScoreDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            String roundIdStr = req.getParameter("roundId");
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

            // Load all teams for this quiz
            List<Team> teams = teamDAO.findByQuizCode(round.getQuizCode());

            // Load existing scores for this round
            Map<String, Score> existingScores = scoreDAO.findByRound(roundId);

            req.setAttribute("round", round);
            req.setAttribute("teams", teams);
            req.setAttribute("existingScores", existingScores);

            req.getRequestDispatcher("/WEB-INF/views/score-entry.jsp").forward(req, resp);

        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("error", "Failed to load score entry: " + e.getMessage());
            req.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        try {
            String roundIdStr = req.getParameter("roundId");
            if (roundIdStr == null) {
                resp.sendRedirect(req.getContextPath() + "/admin/dashboard");
                return;
            }

            int roundId = Integer.parseInt(roundIdStr);
            Round round = roundDAO.findById(roundId);

            if (round == null) {
                resp.sendRedirect(req.getContextPath() + "/admin/dashboard");
                return;
            }

            // Don't allow score entry if round is finished
            if (round.isFinished()) {
                resp.sendRedirect(req.getContextPath() + "/admin/score-entry?roundId=" + roundId + "&error=Round is finished");
                return;
            }

            int adminId = (int) req.getSession().getAttribute("adminId");

            // Collect scores from form: each team has a parameter "score_UNIQUEID"
            List<Team> teams = teamDAO.findByQuizCode(round.getQuizCode());
            List<Score> scores = new ArrayList<>();

            for (Team team : teams) {
                String pointsStr = req.getParameter("score_" + team.getUniqueId());
                if (pointsStr != null && !pointsStr.trim().isEmpty()) {
                    try {
                        double points = Double.parseDouble(pointsStr.trim());
                        if (points < 0) {
                            req.setAttribute("error", "Negative points are not allowed for team " + team.getUniqueId());
                            doGet(req, resp);
                            return;
                        }
                        scores.add(new Score(team.getUniqueId(), roundId, points, adminId));
                    } catch (NumberFormatException e) {
                        req.setAttribute("error", "Invalid score value for team " + team.getUniqueId());
                        doGet(req, resp);
                        return;
                    }
                }
            }

            if (!scores.isEmpty()) {
                scoreDAO.batchUpsert(scores);
            }

            resp.sendRedirect(req.getContextPath() + "/admin/score-entry?roundId=" + roundId + "&success=Scores saved");

        } catch (Exception e) {
            e.printStackTrace();
            resp.sendRedirect(req.getContextPath() + "/admin/dashboard?error=Failed to save scores");
        }
    }
}
