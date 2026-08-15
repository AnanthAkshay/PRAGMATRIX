package com.pragmatrix.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Displays the registration success page with the generated unique ID.
 */
@WebServlet(name = "RegistrationSuccessServlet", urlPatterns = {"/registration-success"})
public class RegistrationSuccessServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String uniqueId = req.getParameter("id");
        if (uniqueId == null || uniqueId.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/register");
            return;
        }
        req.setAttribute("uniqueId", uniqueId);
        req.getRequestDispatcher("/registration-success.jsp").forward(req, resp);
    }
}
