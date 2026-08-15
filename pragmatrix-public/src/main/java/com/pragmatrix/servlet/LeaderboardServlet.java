package com.pragmatrix.servlet;

import com.pragmatrix.dao.RoundDAO;
import com.pragmatrix.dao.ScoreDAO;
import com.pragmatrix.model.LeaderboardEntry;
import com.pragmatrix.model.Round;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * Public leaderboard servlet — read-only, no authentication required.
 * GET /leaderboard?quiz=BIZWIZX          → HTML leaderboard page
 * GET /leaderboard?quiz=BIZWIZX&format=json → JSON for AJAX polling
 */
@WebServlet(name = "PublicLeaderboardServlet", urlPatterns = {"/leaderboard"})
public class LeaderboardServlet extends HttpServlet {

    private final ScoreDAO scoreDAO = new ScoreDAO();
    private final RoundDAO roundDAO = new RoundDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            String quizCode = req.getParameter("quiz");
            if (quizCode == null || (!quizCode.equals("BIZWIZX") && !quizCode.equals("VORTEX"))) {
                quizCode = "BIZWIZX";
            }

            String format = req.getParameter("format");

            List<LeaderboardEntry> entries = scoreDAO.getLeaderboard(quizCode);
            List<Round> rounds = roundDAO.findByQuizCode(quizCode);

            if ("json".equals(format)) {
                // AJAX response
                resp.setContentType("application/json");
                resp.setCharacterEncoding("UTF-8");
                PrintWriter out = resp.getWriter();
                out.print(gson.toJson(new LeaderboardData(entries, rounds)));
                out.flush();
            } else {
                // HTML page
                req.setAttribute("entries", entries);
                req.setAttribute("rounds", rounds);
                req.setAttribute("selectedQuiz", quizCode);
                req.getRequestDispatcher("/WEB-INF/views/leaderboard.jsp").forward(req, resp);
            }

        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("error", "Failed to load leaderboard: " + e.getMessage());
            req.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(req, resp);
        }
    }

    /** DTO for JSON serialization */
    private static class LeaderboardData {
        List<LeaderboardEntry> entries;
        List<Round> rounds;

        LeaderboardData(List<LeaderboardEntry> entries, List<Round> rounds) {
            this.entries = entries;
            this.rounds = rounds;
        }
    }
}
