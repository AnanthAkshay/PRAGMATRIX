package com.pragmatrix.servlet;

import com.pragmatrix.dao.QuizDAO;
import com.pragmatrix.dao.RoundDAO;
import com.pragmatrix.dao.ScoreDAO;
import com.pragmatrix.dao.TeamDAO;
import com.pragmatrix.model.LeaderboardEntry;
import com.pragmatrix.model.Quiz;
import com.pragmatrix.model.Round;
import com.pragmatrix.model.Team;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Admin dashboard — central hub for quiz management.
 * Shows quiz switcher tabs, team list, and round management.
 */
@WebServlet(name = "DashboardServlet", urlPatterns = {"/admin/dashboard"})
public class DashboardServlet extends HttpServlet {

    private final QuizDAO quizDAO = new QuizDAO();
    private final TeamDAO teamDAO = new TeamDAO();
    private final RoundDAO roundDAO = new RoundDAO();
    private final ScoreDAO scoreDAO = new ScoreDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            // Determine selected quiz (default: BIZWIZX)
            String quizCode = req.getParameter("quiz");
            if (quizCode == null || (!quizCode.equals("BIZWIZX") && !quizCode.equals("VORTEX"))) {
                quizCode = "BIZWIZX";
            }

            // Load quizzes for tab display
            List<Quiz> quizzes = quizDAO.findAll();

            // Load teams for selected quiz
            String searchQuery = req.getParameter("search");
            List<Team> teams;
            if (searchQuery != null && !searchQuery.trim().isEmpty()) {
                teams = teamDAO.searchTeams(quizCode, searchQuery.trim());
            } else {
                teams = teamDAO.findByQuizCode(quizCode);
            }

            // Load rounds for selected quiz
            List<Round> rounds = roundDAO.findByQuizCode(quizCode);

            // Team count
            int teamCount = teamDAO.countByQuizCode(quizCode);

            // If BIZWIZX, load ranked elimination standings for finished rounds 2 & 3
            if ("BIZWIZX".equalsIgnoreCase(quizCode)) {
                boolean r2Finished = false;
                boolean r3Finished = false;
                for (Round r : rounds) {
                    if (r.getRoundNumber() == 2 && r.isFinished()) r2Finished = true;
                    if (r.getRoundNumber() == 3 && r.isFinished()) r3Finished = true;
                }

                if (r2Finished) {
                    List<LeaderboardEntry> r2Standings = scoreDAO.getRankedStandings("BIZWIZX", 2, true);
                    boolean hasTieR2 = r2Standings.stream().anyMatch(LeaderboardEntry::isTied);
                    req.setAttribute("round2Standings", r2Standings);
                    req.setAttribute("hasTieRound2", hasTieR2);
                }

                if (r3Finished) {
                    List<LeaderboardEntry> r3Standings = scoreDAO.getRankedStandings("BIZWIZX", 3, true);
                    boolean hasTieR3 = r3Standings.stream().anyMatch(LeaderboardEntry::isTied);
                    req.setAttribute("round3Standings", r3Standings);
                    req.setAttribute("hasTieRound3", hasTieR3);
                }
            } else if ("VORTEX".equalsIgnoreCase(quizCode)) {
                boolean enmaFinished = false;
                for (Round r : rounds) {
                    if (r.getRoundNumber() == 3 && r.isFinished()) {
                        enmaFinished = true;
                        break;
                    }
                }

                if (enmaFinished) {
                    List<LeaderboardEntry> vortexStandings = scoreDAO.getRankedStandings("VORTEX", 3, true);

                    // Populate advanced status from teams
                    Map<String, Boolean> advancedMap = new HashMap<>();
                    for (Team t : teams) {
                        advancedMap.put(t.getUniqueId(), t.isAdvancedToFinale());
                    }
                    for (LeaderboardEntry entry : vortexStandings) {
                        entry.setAdvanced(advancedMap.getOrDefault(entry.getUniqueId(), false));
                    }

                    // Detect tie at the 3rd place cutoff (between 3rd team and 4th team)
                    boolean hasTieAtCutoff = false;
                    if (vortexStandings.size() >= 4) {
                        double rank3Total = vortexStandings.get(2).getTotalPoints();
                        double rank4Total = vortexStandings.get(3).getTotalPoints();
                        if (Double.compare(rank3Total, rank4Total) == 0) {
                            hasTieAtCutoff = true;
                        }
                    }

                    req.setAttribute("vortexFinaleStandings", vortexStandings);
                    req.setAttribute("hasTieAtCutoffVortex", hasTieAtCutoff);
                }
            }

            // Set attributes
            req.setAttribute("quizzes", quizzes);
            req.setAttribute("selectedQuiz", quizCode);
            req.setAttribute("teams", teams);
            req.setAttribute("rounds", rounds);
            req.setAttribute("teamCount", teamCount);
            req.setAttribute("searchQuery", searchQuery);

            req.getRequestDispatcher("/WEB-INF/views/dashboard.jsp").forward(req, resp);

        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("error", "Failed to load dashboard: " + e.getMessage());
            req.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(req, resp);
        }
    }
}
