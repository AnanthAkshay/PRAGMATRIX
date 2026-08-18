package com.pragmatrix.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Authentication filter that guards protected admin routes.
 * Whitelists the public landing page (/admin, /admin/) and auth routes (/admin/login, /admin/otp-verify).
 * For protected routes (/admin/dashboard, /admin/scores, etc.), checks for a valid admin session
 * and redirects unauthenticated users to the admin login page.
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

        String path = httpReq.getServletPath();
        if (path == null) {
            path = "";
        }

        // Whitelist public landing page and login/verify endpoints under /admin
        if ("/admin".equals(path) || "/admin/".equals(path)
                || "/admin/login".equals(path) || "/admin/otp-verify".equals(path)) {
            chain.doFilter(request, response);
            return;
        }

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
