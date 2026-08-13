package com.pragmatrix.servlet;

import com.pragmatrix.dao.QuizDAO;
import com.pragmatrix.dao.TeamDAO;
import com.pragmatrix.model.Quiz;
import com.pragmatrix.model.Team;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Handles team registration.
 * GET  /register → display registration form
 * POST /register → validate, insert team, redirect to success
 */
@WebServlet(name = "RegisterServlet", urlPatterns = {"/register"})
public class RegisterServlet extends HttpServlet {

    private final TeamDAO teamDAO = new TeamDAO();
    private final QuizDAO quizDAO = new QuizDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.getRequestDispatcher("/register.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        String quizCode = req.getParameter("quizCode");
        String collegeName = trim(req.getParameter("collegeName"));
        String student1 = trim(req.getParameter("student1Name"));
        String student2 = trim(req.getParameter("student2Name"));
        String student3 = trim(req.getParameter("student3Name"));

        // --- Server-side validation ---
        StringBuilder errors = new StringBuilder();

        if (quizCode == null || (!quizCode.equals("BIZWIZX") && !quizCode.equals("VORTEX"))) {
            errors.append("Please select a valid quiz event. ");
        }
        if (collegeName == null || collegeName.isEmpty()) {
            errors.append("College name is required. ");
        }
        if (student1 == null || student1.isEmpty()) {
            errors.append("At least one student name is required. ");
        }

        if (errors.length() > 0) {
            req.setAttribute("error", errors.toString().trim());
            req.setAttribute("quizCode", quizCode);
            req.setAttribute("collegeName", collegeName);
            req.setAttribute("student1Name", student1);
            req.setAttribute("student2Name", student2);
            req.setAttribute("student3Name", student3);
            req.getRequestDispatcher("/register.jsp").forward(req, resp);
            return;
        }

        try {
            Quiz quiz = quizDAO.findByCode(quizCode);
            if (quiz == null) {
                req.setAttribute("error", "Invalid quiz selected.");
                req.getRequestDispatcher("/register.jsp").forward(req, resp);
                return;
            }

            Team team = new Team(quizCode, collegeName, student1,
                                 emptyToNull(student2), emptyToNull(student3));

            String uniqueId = teamDAO.insert(team, quiz.getIdPrefix());

            // Redirect to success page with the generated ID
            resp.sendRedirect(req.getContextPath() + "/registration-success?id=" + uniqueId);

        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("error", "Registration failed. Please try again. (" + e.getMessage() + ")");
            req.getRequestDispatcher("/register.jsp").forward(req, resp);
        }
    }

    private String trim(String s) {
        return (s == null) ? null : s.trim();
    }

    private String emptyToNull(String s) {
        return (s == null || s.isEmpty()) ? null : s;
    }
}
