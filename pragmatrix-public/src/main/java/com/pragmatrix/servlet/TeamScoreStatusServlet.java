package com.pragmatrix.servlet;

import com.pragmatrix.dao.RoundDAO;
import com.pragmatrix.dao.ScoreDAO;
import com.pragmatrix.dao.TeamDAO;
import com.pragmatrix.model.Round;
import com.pragmatrix.model.Score;
import com.pragmatrix.model.Team;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.*;

/**
 * JSON endpoint for AJAX polling from the Team Dashboard.
 * Returns current round-wise scores, total points, and leaderboard rank.
 *
 * GET /team/score-status → JSON response (requires valid team session)
 */
@WebServlet(name = "TeamScoreStatusServlet", urlPatterns = {"/team/score-status"})
public class TeamScoreStatusServlet extends HttpServlet {

    private final TeamDAO teamDAO = new TeamDAO();
    private final RoundDAO roundDAO = new RoundDAO();
    private final ScoreDAO scoreDAO = new ScoreDAO();
    private final Gson gson = new GsonBuilder().create();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("teamUniqueId") == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            resp.getWriter().write("{\"error\":\"Not authenticated\"}");
            return;
        }

        String teamCode = (String) session.getAttribute("teamUniqueId");

        try {
            Team team = teamDAO.findByUniqueId(teamCode);
            if (team == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.setContentType("application/json");
                resp.setCharacterEncoding("UTF-8");
                resp.getWriter().write("{\"error\":\"Team not found\"}");
                return;
            }

            // Get rounds for this team's quiz
            List<Round> rounds = roundDAO.findByQuizCode(team.getQuizCode());

            // Get this team's scores (keyed by round_id)
            Map<Integer, Score> scoreMap = scoreDAO.findByTeam(teamCode);

            // Build response
            List<Map<String, Object>> roundData = new ArrayList<>();
            double totalPoints = 0;

            for (Round round : rounds) {
                Map<String, Object> rd = new LinkedHashMap<>();
                rd.put("roundNumber", round.getRoundNumber());
                rd.put("roundName", round.getRoundName());
                rd.put("judgingCriteria", round.getJudgingCriteria());
                rd.put("isFinished", round.isFinished());

                Score score = scoreMap.get(round.getRoundId());
                if (round.isFinished() && score != null) {
                    rd.put("points", score.getPoints());
                    totalPoints += score.getPoints();
                } else {
                    rd.put("points", null); // will appear as "Pending" on frontend
                }

                roundData.add(rd);
            }

            // Calculate rank within quiz
            int rank = calculateRank(team.getQuizCode(), teamCode, totalPoints);

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("rounds", roundData);
            result.put("totalPoints", totalPoints);
            result.put("rank", rank);
            result.put("lastUpdated", System.currentTimeMillis());

            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            resp.getWriter().write(gson.toJson(result));

        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            resp.getWriter().write("{\"error\":\"Internal server error\"}");
        }
    }

    /**
     * Calculate the rank of a team within its quiz based on total points.
     * Uses the leaderboard view query pattern.
     */
    private int calculateRank(String quizCode, String teamCode, double teamTotalPoints) {
        try {
            List<Team> allTeams = teamDAO.findByQuizCode(quizCode);
            // Sort by total points descending
            allTeams.sort((a, b) -> Double.compare(b.getTotalPoints(), a.getTotalPoints()));

            for (int i = 0; i < allTeams.size(); i++) {
                if (allTeams.get(i).getUniqueId().equals(teamCode)) {
                    return i + 1;
                }
            }
        } catch (Exception e) {
            System.err.println("[PRAGMATRIX] Error calculating rank for " + teamCode + ": " + e.getMessage());
        }
        return 0; // unknown
    }
}
