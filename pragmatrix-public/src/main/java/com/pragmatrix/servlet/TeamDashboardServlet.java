package com.pragmatrix.servlet;

import com.pragmatrix.dao.RoundDAO;
import com.pragmatrix.dao.ScoreDAO;
import com.pragmatrix.dao.TeamDAO;
import com.pragmatrix.dao.VortexCriteriaDAO;
import com.pragmatrix.model.Round;
import com.pragmatrix.model.Score;
import com.pragmatrix.model.Team;
import com.pragmatrix.model.VortexRound;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Team Dashboard — displays team details, round-wise scores, and read-only VORTEX judging criteria.
 * Guarded by TeamAuthFilter (/team/*).
 *
 * GET /team/dashboard → display team dashboard
 */
@WebServlet(name = "TeamDashboardServlet", urlPatterns = {"/team/dashboard"})
public class TeamDashboardServlet extends HttpServlet {

    private final TeamDAO teamDAO = new TeamDAO();
    private final RoundDAO roundDAO = new RoundDAO();
    private final ScoreDAO scoreDAO = new ScoreDAO();
    private final VortexCriteriaDAO vortexDAO = new VortexCriteriaDAO();

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

            // If VORTEX, load VORTEX judging criteria structure and team detailed scores for read-only view ONLY if round is Finished
            if ("VORTEX".equalsIgnoreCase(team.getQuizCode())) {
                Map<Integer, Round> roundMapByNumber = new HashMap<>();
                for (Round r : rounds) {
                    roundMapByNumber.put(r.getRoundNumber(), r);
                }

                List<VortexRound> vRounds = vortexDAO.getAllRounds();
                Map<Integer, VortexRound> vortexRoundsMap = new HashMap<>();
                Map<Integer, Map<Integer, Double>> teamDetailedScores = new HashMap<>();
                for (VortexRound vr : vRounds) {
                    Round r = roundMapByNumber.get(vr.getDisplayOrder());
                    boolean isFinished = (r != null && r.isFinished());
                    boolean isFinaleAndNotAdvanced = (vr.getDisplayOrder() == 4 && !team.isAdvancedToFinale());

                    // Server-side gating: Only serve criteria & scores if round is finished (and if round 4, advanced to finale)
                    if (isFinished && !isFinaleAndNotAdvanced) {
                        vortexRoundsMap.put(vr.getDisplayOrder(), vr);
                        // Strictly scoped to this logged-in team's own data only
                        Map<Integer, Double> cScores = vortexDAO.getTeamScoresForRound(teamCode, vr.getRoundId());
                        teamDetailedScores.put(vr.getDisplayOrder(), cScores);
                    }
                }
                req.setAttribute("vortexRoundsMap", vortexRoundsMap);
                req.setAttribute("teamDetailedScores", teamDetailedScores);
            }

            req.setAttribute("team", team);
            req.setAttribute("rounds", rounds);
            req.setAttribute("scoreMap", scoreMap);
            req.setAttribute("totalPoints", totalPoints);

            req.getRequestDispatcher("/WEB-INF/views/team-dashboard.jsp").forward(req, resp);

        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("error", "Failed to load dashboard: " + e.getMessage());
            req.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(req, resp);
        }
    }
}
