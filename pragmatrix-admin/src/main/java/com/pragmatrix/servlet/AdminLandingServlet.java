package com.pragmatrix.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Serves the PRAGMATRIX Admin Landing Page (Page 1).
 * If the user already has an active admin session, redirects to /admin/dashboard.
 * Otherwise, forwards to the branded landing page (index.jsp).
 */
@WebServlet(name = "AdminLandingServlet", urlPatterns = {"/admin", "/admin/"})
public class AdminLandingServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("adminId") != null) {
            resp.sendRedirect(req.getContextPath() + "/admin/dashboard");
            return;
        }
        req.getRequestDispatcher("/index.jsp").forward(req, resp);
    }
}
