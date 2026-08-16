package com.pragmatrix.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Lightweight health check endpoint for container orchestrators and Render health probes.
 * Returns HTTP 200 OK without requiring database connectivity to ensure minimal overhead.
 */
@WebServlet(name = "HealthCheckServlet", urlPatterns = {"/health", "/healthz"})
public class HealthCheckServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/plain;charset=UTF-8");
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().write("OK");
    }
}
