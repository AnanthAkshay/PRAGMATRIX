package com.pragmatrix.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Authentication filter that guards all team dashboard routes (/team/*).
 * Checks for a valid team session; redirects to team-login if absent.
 */
@WebFilter(urlPatterns = {"/team/*"})
public class TeamAuthFilter implements Filter {

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

        if (session == null || session.getAttribute("teamUniqueId") == null) {
            // Not authenticated — redirect to team login
            httpResp.sendRedirect(httpReq.getContextPath() + "/team-login");
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
