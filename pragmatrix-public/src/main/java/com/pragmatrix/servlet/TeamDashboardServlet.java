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
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Team Dashboard — displays team details and round-wise scores.
 * Guarded by TeamAuthFilter (/team/*).
 *
 * GET /team/dashboard → display team dashboard
 */
@WebServlet(name = "TeamDashboardServlet", urlPatterns = {"/team/dashboard"})
public class TeamDashboardServlet extends HttpServlet {

    private final TeamDAO teamDAO = new TeamDAO();
    private final RoundDAO roundDAO = new RoundDAO();
    private final ScoreDAO scoreDAO = new ScoreDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        String teamCode = (String) session.getAttribute("teamUniqueId");

        try {
            Team team = teamDAO.findByUniqueId(teamCode);
            if (team == null) {
                session.removeAttribute("teamUniqueId");
                resp.sendRedirect(req.getContextPath() + "/team-login");
                return;
            }

            // Get rounds for this quiz
            List<Round> rounds = roundDAO.findByQuizCode(team.getQuizCode());

            // Get this team's scores
            Map<Integer, Score> scoreMap = scoreDAO.findByTeam(teamCode);

            // Calculate total points from finished rounds
            double totalPoints = 0;
            for (Round round : rounds) {
                if (round.isFinished()) {
                    Score score = scoreMap.get(round.getRoundId());
                    if (score != null) {
                        totalPoints += score.getPoints();
                    }
                }
            }

            // Calculate rank
            List<Team> allTeams = teamDAO.findByQuizCode(team.getQuizCode());
            allTeams.sort((a, b) -> Double.compare(b.getTotalPoints(), a.getTotalPoints()));
            int rank = 0;
            for (int i = 0; i < allTeams.size(); i++) {
                if (allTeams.get(i).getUniqueId().equals(teamCode)) {
                    rank = i + 1;
                    break;
                }
            }

            req.setAttribute("team", team);
            req.setAttribute("rounds", rounds);
            req.setAttribute("scoreMap", scoreMap);
            req.setAttribute("totalPoints", totalPoints);
            req.setAttribute("rank", rank);
            req.setAttribute("totalTeams", allTeams.size());

            req.getRequestDispatcher("/WEB-INF/views/team-dashboard.jsp").forward(req, resp);

        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("error", "Failed to load dashboard: " + e.getMessage());
            req.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(req, resp);
        }
    }
}
