package com.pragmatrix.servlet;

import com.pragmatrix.dao.TeamDAO;
import com.pragmatrix.model.Team;
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
 * AJAX endpoint for searching teams by unique ID or college name.
 * GET /admin/team-search?quiz=BIZWIZX&q=search_term
 */
@WebServlet(name = "TeamSearchServlet", urlPatterns = {"/admin/team-search"})
public class TeamSearchServlet extends HttpServlet {

    private final TeamDAO teamDAO = new TeamDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try {
            String quizCode = req.getParameter("quiz");
            String query = req.getParameter("q");

            if (quizCode == null || (!quizCode.equals("BIZWIZX") && !quizCode.equals("VORTEX"))) {
                quizCode = "BIZWIZX";
            }

            List<Team> teams;
            if (query != null && !query.trim().isEmpty()) {
                teams = teamDAO.searchTeams(quizCode, query.trim());
            } else {
                teams = teamDAO.findByQuizCode(quizCode);
            }

            PrintWriter out = resp.getWriter();
            out.print(gson.toJson(teams));
            out.flush();

        } catch (Exception e) {
            resp.setStatus(500);
            PrintWriter out = resp.getWriter();
            out.print("{\"error\":\"" + e.getMessage() + "\"}");
            out.flush();
        }
    }
}
