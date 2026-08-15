package com.pragmatrix.servlet;

import com.pragmatrix.dao.QuizDAO;
import com.pragmatrix.dao.RoundDAO;
import com.pragmatrix.dao.TeamDAO;
import com.pragmatrix.model.Quiz;
import com.pragmatrix.model.Round;
import com.pragmatrix.model.Team;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * Admin dashboard — central hub for quiz management.
 * Shows quiz switcher tabs, team list, and round management.
 */
@WebServlet(name = "DashboardServlet", urlPatterns = {"/admin/dashboard"})
public class DashboardServlet extends HttpServlet {

    private final QuizDAO quizDAO = new QuizDAO();
    private final TeamDAO teamDAO = new TeamDAO();
    private final RoundDAO roundDAO = new RoundDAO();

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
