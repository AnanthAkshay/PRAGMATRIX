package com.pragmatrix.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Authentication filter that guards all admin routes.
 * Checks for a valid admin session; redirects to login page if absent.
 */
@WebFilter(urlPatterns = {"/admin/*"})
public class AdminAuthFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // no-op
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpReq = (HttpServletRequest) request;
        HttpServletResponse httpResp = (HttpServletResponse) response;

        HttpSession session = httpReq.getSession(false);

        if (session == null || session.getAttribute("adminId") == null) {
            // Not authenticated — redirect to login
            httpResp.sendRedirect(httpReq.getContextPath() + "/login");
            return;
        }

        // Authenticated — continue
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // no-op
    }
}
