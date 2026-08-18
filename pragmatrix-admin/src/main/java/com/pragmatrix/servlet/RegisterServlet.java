package com.pragmatrix.servlet;

import com.pragmatrix.dao.QuizDAO;
import com.pragmatrix.dao.TeamDAO;
import com.pragmatrix.model.Quiz;
import com.pragmatrix.model.Team;
import com.pragmatrix.util.EmailService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Handles team registration (admin-only).
 * POST /register → validate, insert team with lead name + email, send ID email, redirect to dashboard
 */
@WebServlet(name = "RegisterServlet", urlPatterns = {"/register"})
public class RegisterServlet extends HttpServlet {

    private final TeamDAO teamDAO = new TeamDAO();
    private final QuizDAO quizDAO = new QuizDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // Registration is now admin-only — redirect to admin dashboard
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("adminId") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        resp.sendRedirect(req.getContextPath() + "/admin/dashboard");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        // Require admin session
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("adminId") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String quizCode = req.getParameter("quizCode");
        String collegeName = trim(req.getParameter("collegeName"));
        String teamLeadName = trim(req.getParameter("teamLeadName"));
        String leadEmail = trim(req.getParameter("leadEmail"));
        String member2Name = trimToNull(req.getParameter("member2Name"));
        String member3Name = trimToNull(req.getParameter("member3Name"));

        // Determine redirect quiz tab
        String redirectQuiz = (quizCode != null) ? quizCode : "BIZWIZX";
        String dashboardUrl = req.getContextPath() + "/admin/dashboard?quiz=" + redirectQuiz;

        // --- Server-side validation ---
        StringBuilder errors = new StringBuilder();

        if (quizCode == null || (!quizCode.equals("BIZWIZX") && !quizCode.equals("VORTEX"))) {
            errors.append("Please select a valid quiz event. ");
        }
        if (collegeName == null || collegeName.isEmpty()) {
            errors.append("College name is required. ");
        }
        if (teamLeadName == null || teamLeadName.isEmpty()) {
            errors.append("Team lead name is required. ");
        }
        if (leadEmail == null || leadEmail.isEmpty()) {
            errors.append("Team lead email is required. ");
        } else if (!isValidEmail(leadEmail)) {
            errors.append("Please enter a valid email address. ");
        }

        if (errors.length() > 0) {
            resp.sendRedirect(dashboardUrl + "&error=" + URLEncoder.encode(errors.toString().trim(), StandardCharsets.UTF_8));
            return;
        }

        try {
            Quiz quiz = quizDAO.findByCode(quizCode);
            if (quiz == null) {
                resp.sendRedirect(dashboardUrl + "&error=" + URLEncoder.encode("Invalid quiz selected.", StandardCharsets.UTF_8));
                return;
            }

            Team team = new Team(quizCode, collegeName, teamLeadName, leadEmail, member2Name, member3Name);

            String uniqueId = teamDAO.insert(team, quiz.getIdPrefix());

            // Attempt to send the registration confirmation email
            boolean emailSent = EmailService.sendRegistrationConfirmationEmail(leadEmail, teamLeadName, collegeName, quiz.getQuizName(), uniqueId);

            String successMsg = "Team " + uniqueId + " created successfully!";
            if (emailSent) {
                successMsg += " Email sent \u2714";
            } else {
                successMsg += " Email failed \u2718 (check server logs)";
            }

            resp.sendRedirect(dashboardUrl + "&success=" + URLEncoder.encode(successMsg, StandardCharsets.UTF_8));

        } catch (Exception e) {
            e.printStackTrace();
            resp.sendRedirect(dashboardUrl + "&error=" + URLEncoder.encode("Registration failed: " + e.getMessage(), StandardCharsets.UTF_8));
        }
    }

    private String trim(String s) {
        return (s == null) ? null : s.trim();
    }

    private String trimToNull(String s) {
        if (s == null) return null;
        String trimmed = s.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * Basic email format validation.
     */
    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }
}
