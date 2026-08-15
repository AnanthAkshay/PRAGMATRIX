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
import java.util.List;
import java.util.Map;

/**
 * Displays an individual team's scorecard.
 * GET /admin/scorecard?id=PMBZ001
 */
@WebServlet(name = "ScorecardServlet", urlPatterns = {"/admin/scorecard"})
public class ScorecardServlet extends HttpServlet {

    private final TeamDAO teamDAO = new TeamDAO();
    private final RoundDAO roundDAO = new RoundDAO();
    private final ScoreDAO scoreDAO = new ScoreDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            String uniqueId = req.getParameter("id");
            if (uniqueId == null || uniqueId.trim().isEmpty()) {
                resp.sendRedirect(req.getContextPath() + "/admin/dashboard");
                return;
            }

            Team team = teamDAO.findByUniqueId(uniqueId.trim());
            if (team == null) {
                req.setAttribute("error", "Team not found: " + uniqueId);
                req.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(req, resp);
                return;
            }

            // Load rounds for this quiz
            List<Round> rounds = roundDAO.findByQuizCode(team.getQuizCode());

            // Load scores for this team (roundId → Score)
            Map<Integer, Score> scores = scoreDAO.findByTeam(uniqueId.trim());

            // Calculate total (from finished rounds only)
            double total = 0;
            for (Round r : rounds) {
                if (r.isFinished()) {
                    Score s = scores.get(r.getRoundId());
                    if (s != null) {
                        total += s.getPoints();
                    }
                }
            }

            req.setAttribute("team", team);
            req.setAttribute("rounds", rounds);
            req.setAttribute("scores", scores);
            req.setAttribute("totalPoints", total);

            req.getRequestDispatcher("/WEB-INF/views/scorecard.jsp").forward(req, resp);

        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("error", "Failed to load scorecard: " + e.getMessage());
            req.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(req, resp);
        }
    }
}
