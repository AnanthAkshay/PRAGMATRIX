package com.pragmatrix.servlet;

import com.pragmatrix.dao.TeamDAO;
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
 * Admin action: resend the Participant ID email to a team's lead email.
 * Requires a valid admin session.
 *
 * POST /admin/resend-email?uniqueId=PMBZ001&quiz=BIZWIZX
 */
@WebServlet(name = "ResendIdEmailServlet", urlPatterns = {"/admin/resend-email"})
public class ResendIdEmailServlet extends HttpServlet {

    private final TeamDAO teamDAO = new TeamDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        // Admin session is already enforced by AdminAuthFilter (/admin/*)

        String uniqueId = req.getParameter("uniqueId");
        String quizCode = req.getParameter("quiz");
        if (quizCode == null) quizCode = "BIZWIZX";

        String redirectUrl = req.getContextPath() + "/admin/dashboard?quiz=" + quizCode;

        if (uniqueId == null || uniqueId.trim().isEmpty()) {
            resp.sendRedirect(redirectUrl + "&error=" + URLEncoder.encode("Missing team ID.", StandardCharsets.UTF_8));
            return;
        }

        try {
            com.pragmatrix.model.Team team = teamDAO.findByUniqueId(uniqueId.trim());
            if (team == null || team.getLeadEmail() == null) {
                resp.sendRedirect(redirectUrl + "&error=" + URLEncoder.encode("Team not found: " + uniqueId, StandardCharsets.UTF_8));
                return;
            }

            boolean sent = EmailService.sendRegistrationConfirmationEmail(team.getLeadEmail(), team.getTeamLeadName(), team.getCollegeName(), team.getQuizCode(), uniqueId.trim());
            if (sent) {
                resp.sendRedirect(redirectUrl + "&success=" + URLEncoder.encode("Confirmation email resent to " + uniqueId + " ✔", StandardCharsets.UTF_8));
            } else {
                resp.sendRedirect(redirectUrl + "&error=" + URLEncoder.encode("Email sending failed for " + uniqueId + ". Check server logs.", StandardCharsets.UTF_8));
            }

        } catch (Exception e) {
            e.printStackTrace();
            resp.sendRedirect(redirectUrl + "&error=" + URLEncoder.encode("Error: " + e.getMessage(), StandardCharsets.UTF_8));
        }
    }
}
