package com.pragmatrix.servlet;

import com.pragmatrix.dao.RoundDAO;
import com.pragmatrix.dao.ScoreDAO;
import com.pragmatrix.dao.TeamDAO;
import com.pragmatrix.dao.VortexCriteriaDAO;
import com.pragmatrix.model.JudgingComponent;
import com.pragmatrix.model.JudgingCriterion;
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
import java.util.*;

/**
 * Score entry screen for rounds.
 * Supports VORTEX detailed criteria score entry and BIZWIZX simple marks-only entry.
 */
@WebServlet(name = "ScoreEntryServlet", urlPatterns = {"/admin/score-entry"})
public class ScoreEntryServlet extends HttpServlet {

    private final RoundDAO roundDAO = new RoundDAO();
    private final TeamDAO teamDAO = new TeamDAO();
    private final ScoreDAO scoreDAO = new ScoreDAO();
    private final VortexCriteriaDAO vortexDAO = new VortexCriteriaDAO();

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

            List<Team> teams = teamDAO.findByQuizCode(round.getQuizCode());
            Map<String, Score> existingScores = scoreDAO.findByRound(roundId);

            if ("BIZWIZX".equalsIgnoreCase(round.getQuizCode()) && round.getRoundNumber() > 1) {
                List<Team> eligibleTeams = new ArrayList<>();
                for (Team t : teams) {
                    if (!t.isEliminated() || existingScores.containsKey(t.getUniqueId())) {
                        eligibleTeams.add(t);
                    }
                }
                teams = eligibleTeams;
            }

            req.setAttribute("round", round);
            req.setAttribute("teams", teams);
            req.setAttribute("existingScores", existingScores);

            if ("VORTEX".equalsIgnoreCase(round.getQuizCode())) {
                VortexRound vortexRound = vortexDAO.getRoundByName(round.getRoundName());
                if (vortexRound == null) {
                    // Fallback search by display_order / round_number
                    vortexRound = vortexDAO.getRoundById(round.getRoundNumber());
                }
                req.setAttribute("vortexRound", vortexRound);

                if (vortexRound != null) {
                    Map<String, Map<Integer, Double>> teamCriterionScores = new HashMap<>();
                    for (Team t : teams) {
                        Map<Integer, Double> cScores = vortexDAO.getTeamScoresForRound(t.getUniqueId(), vortexRound.getRoundId());
                        teamCriterionScores.put(t.getUniqueId(), cScores);
                    }
                    req.setAttribute("teamCriterionScores", teamCriterionScores);
                }
            }

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
        HttpSession session = req.getSession(false);

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

            if (round.isFinished()) {
                resp.sendRedirect(req.getContextPath() + "/admin/score-entry?roundId=" + roundId + "&error=Round+is+finished");
                return;
            }

            int adminId = (session != null && session.getAttribute("adminId") != null) ? (int) session.getAttribute("adminId") : 1;
            String adminName = (session != null && session.getAttribute("adminName") != null) ? (String) session.getAttribute("adminName") : "Admin";

            List<Team> teams = teamDAO.findByQuizCode(round.getQuizCode());

            if ("VORTEX".equalsIgnoreCase(round.getQuizCode())) {
                VortexRound vortexRound = vortexDAO.getRoundByName(round.getRoundName());
                if (vortexRound == null) {
                    vortexRound = vortexDAO.getRoundById(round.getRoundNumber());
                }

                if (vortexRound != null && !vortexRound.getComponents().isEmpty()) {
                    for (Team team : teams) {
                        Map<Integer, Double> critScores = new HashMap<>();
                        for (JudgingComponent comp : vortexRound.getComponents()) {
                            for (JudgingCriterion crit : comp.getCriteria()) {
                                String valStr = req.getParameter("score_" + team.getUniqueId() + "_" + crit.getCriterionId());
                                if (valStr != null && !valStr.trim().isEmpty()) {
                                    try {
                                        double scoreVal = Double.parseDouble(valStr.trim());
                                        if (scoreVal < 0 || scoreVal > crit.getMaxMarks()) {
                                            resp.sendRedirect(req.getContextPath() + "/admin/score-entry?roundId=" + roundId + "&error=Score+for+" + team.getUniqueId() + "+exceeds+max+marks+of+" + crit.getMaxMarks());
                                            return;
                                        }
                                        critScores.put(crit.getCriterionId(), scoreVal);
                                    } catch (NumberFormatException nfe) {
                                        resp.sendRedirect(req.getContextPath() + "/admin/score-entry?roundId=" + roundId + "&error=Invalid+numeric+score+entered+for+" + team.getUniqueId());
                                        return;
                                    }
                                }
                            }
                        }
                        if (!critScores.isEmpty()) {
                            boolean saved = vortexDAO.saveTeamScores(team.getUniqueId(), vortexRound.getRoundId(), roundId, critScores, adminName);
                            if (!saved) {
                                resp.sendRedirect(req.getContextPath() + "/admin/score-entry?roundId=" + roundId + "&error=Database+error+saving+scores+for+" + team.getUniqueId());
                                return;
                            }
                        }
                    }
                } else {
                    resp.sendRedirect(req.getContextPath() + "/admin/score-entry?roundId=" + roundId + "&error=No+judging+criteria+configured+for+this+round.+Please+manage+criteria+first.");
                    return;
                }
            } else {
                // BIZWIZX Simple Score Entry
                List<Score> scores = new ArrayList<>();
                for (Team team : teams) {
                    if (round.getRoundNumber() > 1 && team.isEliminated()) {
                        continue;
                    }
                    String pointsStr = req.getParameter("score_" + team.getUniqueId());
                    if (pointsStr != null && !pointsStr.trim().isEmpty()) {
                        try {
                            double points = Double.parseDouble(pointsStr.trim());
                            if (points >= 0) {
                                scores.add(new Score(team.getUniqueId(), roundId, points, adminId));
                            }
                        } catch (NumberFormatException nfe) {
                            resp.sendRedirect(req.getContextPath() + "/admin/score-entry?roundId=" + roundId + "&error=Invalid+numeric+score+entered+for+" + team.getUniqueId());
                            return;
                        }
                    }
                }
                if (!scores.isEmpty()) {
                    scoreDAO.batchUpsert(scores);
                }
            }

            resp.sendRedirect(req.getContextPath() + "/admin/score-entry?roundId=" + roundId + "&success=Scores+saved+successfully");

        } catch (Exception e) {
            e.printStackTrace();
            String roundIdParam = req.getParameter("roundId");
            if (roundIdParam != null && !roundIdParam.trim().isEmpty()) {
                resp.sendRedirect(req.getContextPath() + "/admin/score-entry?roundId=" + roundIdParam.trim() + "&error=Failed+to+save+scores");
            } else {
                resp.sendRedirect(req.getContextPath() + "/admin/dashboard?error=Failed+to+save+scores");
            }
        }
    }
}
