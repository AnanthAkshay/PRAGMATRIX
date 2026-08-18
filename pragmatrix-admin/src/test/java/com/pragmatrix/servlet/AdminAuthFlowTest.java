package com.pragmatrix.servlet;

import com.pragmatrix.dao.AdminDAO;
import com.pragmatrix.filter.AdminAuthFilter;
import com.pragmatrix.model.Admin;
import jakarta.servlet.FilterChain;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class AdminAuthFlowTest {

    // Helper to create mock HttpSession
    private HttpSession createMockSession() {
        Map<String, Object> attributes = new HashMap<>();
        return (HttpSession) Proxy.newProxyInstance(
                HttpSession.class.getClassLoader(),
                new Class<?>[]{HttpSession.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("getAttribute".equals(name)) {
                        return attributes.get(args[0]);
                    } else if ("setAttribute".equals(name)) {
                        attributes.put((String) args[0], args[1]);
                        return null;
                    } else if ("removeAttribute".equals(name)) {
                        return attributes.remove(args[0]);
                    } else if ("setMaxInactiveInterval".equals(name)) {
                        return null;
                    } else if ("invalidate".equals(name)) {
                        attributes.clear();
                        return null;
                    }
                    return null;
                }
        );
    }

    @Test
    @DisplayName("AdminAuthFilter allows public landing page /admin and /admin/ without session")
    void testFilterAllowsAdminLanding() throws ServletException, IOException {
        AdminAuthFilter filter = new AdminAuthFilter();
        AtomicBoolean chainCalled = new AtomicBoolean(false);

        HttpServletRequest req = (HttpServletRequest) Proxy.newProxyInstance(
                HttpServletRequest.class.getClassLoader(),
                new Class<?>[]{HttpServletRequest.class},
                (proxy, method, args) -> {
                    if ("getServletPath".equals(method.getName())) return "/admin";
                    if ("getSession".equals(method.getName())) return null;
                    return null;
                }
        );

        HttpServletResponse resp = (HttpServletResponse) Proxy.newProxyInstance(
                HttpServletResponse.class.getClassLoader(),
                new Class<?>[]{HttpServletResponse.class},
                (proxy, method, args) -> null
        );

        FilterChain chain = (request, response) -> chainCalled.set(true);

        filter.doFilter(req, resp, chain);
        assertTrue(chainCalled.get(), "FilterChain must be invoked for public /admin landing page");
    }

    @Test
    @DisplayName("AdminAuthFilter blocks /admin/dashboard when session is absent and redirects to /login")
    void testFilterBlocksProtectedAdminDashboard() throws ServletException, IOException {
        AdminAuthFilter filter = new AdminAuthFilter();
        AtomicBoolean chainCalled = new AtomicBoolean(false);
        AtomicReference<String> redirectedUrl = new AtomicReference<>();

        HttpServletRequest req = (HttpServletRequest) Proxy.newProxyInstance(
                HttpServletRequest.class.getClassLoader(),
                new Class<?>[]{HttpServletRequest.class},
                (proxy, method, args) -> {
                    if ("getServletPath".equals(method.getName())) return "/admin/dashboard";
                    if ("getContextPath".equals(method.getName())) return "/pragmatrix";
                    if ("getSession".equals(method.getName())) return null;
                    return null;
                }
        );

        HttpServletResponse resp = (HttpServletResponse) Proxy.newProxyInstance(
                HttpServletResponse.class.getClassLoader(),
                new Class<?>[]{HttpServletResponse.class},
                (proxy, method, args) -> {
                    if ("sendRedirect".equals(method.getName())) {
                        redirectedUrl.set((String) args[0]);
                    }
                    return null;
                }
        );

        FilterChain chain = (request, response) -> chainCalled.set(true);

        filter.doFilter(req, resp, chain);
        assertFalse(chainCalled.get(), "FilterChain must not be called when unauthenticated");
        assertEquals("/pragmatrix/login", redirectedUrl.get());
    }

    @Test
    @DisplayName("AdminLandingServlet forwards unauthenticated user to index.jsp")
    void testAdminLandingServletForwardsUnauthenticated() throws ServletException, IOException {
        AdminLandingServlet servlet = new AdminLandingServlet();
        AtomicReference<String> forwardedPath = new AtomicReference<>();

        RequestDispatcher dispatcher = (RequestDispatcher) Proxy.newProxyInstance(
                RequestDispatcher.class.getClassLoader(),
                new Class<?>[]{RequestDispatcher.class},
                (proxy, method, args) -> {
                    if ("forward".equals(method.getName())) {
                        // success
                    }
                    return null;
                }
        );

        HttpServletRequest req = (HttpServletRequest) Proxy.newProxyInstance(
                HttpServletRequest.class.getClassLoader(),
                new Class<?>[]{HttpServletRequest.class},
                (proxy, method, args) -> {
                    if ("getSession".equals(method.getName())) return null;
                    if ("getRequestDispatcher".equals(method.getName())) {
                        forwardedPath.set((String) args[0]);
                        return dispatcher;
                    }
                    return null;
                }
        );

        HttpServletResponse resp = (HttpServletResponse) Proxy.newProxyInstance(
                HttpServletResponse.class.getClassLoader(),
                new Class<?>[]{HttpServletResponse.class},
                (proxy, method, args) -> null
        );

        servlet.doGet(req, resp);
        assertEquals("/index.jsp", forwardedPath.get());
    }

    @Test
    @DisplayName("AdminLandingServlet redirects authenticated user to /admin/dashboard")
    void testAdminLandingServletRedirectsAuthenticated() throws ServletException, IOException {
        AdminLandingServlet servlet = new AdminLandingServlet();
        HttpSession session = createMockSession();
        session.setAttribute("adminId", 1);
        AtomicReference<String> redirectUrl = new AtomicReference<>();

        HttpServletRequest req = (HttpServletRequest) Proxy.newProxyInstance(
                HttpServletRequest.class.getClassLoader(),
                new Class<?>[]{HttpServletRequest.class},
                (proxy, method, args) -> {
                    if ("getSession".equals(method.getName())) return session;
                    if ("getContextPath".equals(method.getName())) return "";
                    return null;
                }
        );

        HttpServletResponse resp = (HttpServletResponse) Proxy.newProxyInstance(
                HttpServletResponse.class.getClassLoader(),
                new Class<?>[]{HttpServletResponse.class},
                (proxy, method, args) -> {
                    if ("sendRedirect".equals(method.getName())) {
                        redirectUrl.set((String) args[0]);
                    }
                    return null;
                }
        );

        servlet.doGet(req, resp);
        assertEquals("/admin/dashboard", redirectUrl.get());
    }

    @Test
    @DisplayName("AdminLoginServlet rejects unauthorized email and stays on login page")
    void testAdminLoginRejectsUnauthorizedEmail() throws ServletException, IOException {
        AdminLoginServlet servlet = new AdminLoginServlet();
        Map<String, Object> reqAttrs = new HashMap<>();
        AtomicReference<String> forwardedPath = new AtomicReference<>();

        RequestDispatcher dispatcher = (RequestDispatcher) Proxy.newProxyInstance(
                RequestDispatcher.class.getClassLoader(),
                new Class<?>[]{RequestDispatcher.class},
                (proxy, method, args) -> null
        );

        HttpServletRequest req = (HttpServletRequest) Proxy.newProxyInstance(
                HttpServletRequest.class.getClassLoader(),
                new Class<?>[]{HttpServletRequest.class},
                (proxy, method, args) -> {
                    if ("getParameter".equals(method.getName())) {
                        if ("email".equals(args[0])) return "unauthorized@test.com";
                        return null;
                    }
                    if ("setAttribute".equals(method.getName())) {
                        reqAttrs.put((String) args[0], args[1]);
                        return null;
                    }
                    if ("getRequestDispatcher".equals(method.getName())) {
                        forwardedPath.set((String) args[0]);
                        return dispatcher;
                    }
                    return null;
                }
        );

        HttpServletResponse resp = (HttpServletResponse) Proxy.newProxyInstance(
                HttpServletResponse.class.getClassLoader(),
                new Class<?>[]{HttpServletResponse.class},
                (proxy, method, args) -> null
        );

        servlet.doPost(req, resp);
        assertEquals("Unable to process this request.", reqAttrs.get("error"));
        assertEquals("/admin-login.jsp", forwardedPath.get());
    }

    @Test
    @DisplayName("AdminOtpVerifyServlet validates correct OTP, sets admin session and redirects to dashboard")
    void testAdminOtpVerifySuccess() throws ServletException, IOException {
        AdminDAO mockAdminDAO = new AdminDAO() {
            @Override
            public Admin findByEmail(String email) {
                Admin a = new Admin();
                a.setAdminId(42);
                a.setFullName("Super Admin");
                a.setEmail(email);
                a.setUsername("superadmin");
                return a;
            }
        };

        AdminOtpVerifyServlet servlet = new AdminOtpVerifyServlet(mockAdminDAO);
        HttpSession session = createMockSession();
        session.setAttribute("admin_pending_email", "ananthakshay2006@gmail.com");
        session.setAttribute("admin_otp_code", "123456");
        session.setAttribute("admin_otp_expiry", System.currentTimeMillis() + 300000);

        AtomicReference<String> redirectedUrl = new AtomicReference<>();

        HttpServletRequest req = (HttpServletRequest) Proxy.newProxyInstance(
                HttpServletRequest.class.getClassLoader(),
                new Class<?>[]{HttpServletRequest.class},
                (proxy, method, args) -> {
                    if ("getSession".equals(method.getName())) return session;
                    if ("getParameter".equals(method.getName())) {
                        if ("otp".equals(args[0])) return "123456";
                        return null;
                    }
                    if ("getContextPath".equals(method.getName())) return "";
                    return null;
                }
        );

        HttpServletResponse resp = (HttpServletResponse) Proxy.newProxyInstance(
                HttpServletResponse.class.getClassLoader(),
                new Class<?>[]{HttpServletResponse.class},
                (proxy, method, args) -> {
                    if ("sendRedirect".equals(method.getName())) {
                        redirectedUrl.set((String) args[0]);
                    }
                    return null;
                }
        );

        servlet.doPost(req, resp);
        assertEquals("/admin/dashboard", redirectedUrl.get());
        assertEquals(42, session.getAttribute("adminId"));
        assertEquals("Super Admin", session.getAttribute("adminName"));
        assertEquals("ananthakshay2006@gmail.com", session.getAttribute("adminEmail"));
        assertNull(session.getAttribute("admin_otp_code"), "OTP code should be cleared from session");
    }

    @Test
    @DisplayName("AdminOtpVerifyServlet rejects wrong OTP and returns error")
    void testAdminOtpVerifyWrongOtp() throws ServletException, IOException {
        AdminOtpVerifyServlet servlet = new AdminOtpVerifyServlet();
        HttpSession session = createMockSession();
        session.setAttribute("admin_pending_email", "ananthakshay2006@gmail.com");
        session.setAttribute("admin_otp_code", "123456");
        session.setAttribute("admin_otp_expiry", System.currentTimeMillis() + 300000);

        Map<String, Object> reqAttrs = new HashMap<>();
        AtomicReference<String> forwardedPath = new AtomicReference<>();

        RequestDispatcher dispatcher = (RequestDispatcher) Proxy.newProxyInstance(
                RequestDispatcher.class.getClassLoader(),
                new Class<?>[]{RequestDispatcher.class},
                (proxy, method, args) -> null
        );

        HttpServletRequest req = (HttpServletRequest) Proxy.newProxyInstance(
                HttpServletRequest.class.getClassLoader(),
                new Class<?>[]{HttpServletRequest.class},
                (proxy, method, args) -> {
                    if ("getSession".equals(method.getName())) return session;
                    if ("getParameter".equals(method.getName())) {
                        if ("otp".equals(args[0])) return "999999";
                        return null;
                    }
                    if ("setAttribute".equals(method.getName())) {
                        reqAttrs.put((String) args[0], args[1]);
                        return null;
                    }
                    if ("getRequestDispatcher".equals(method.getName())) {
                        forwardedPath.set((String) args[0]);
                        return dispatcher;
                    }
                    return null;
                }
        );

        HttpServletResponse resp = (HttpServletResponse) Proxy.newProxyInstance(
                HttpServletResponse.class.getClassLoader(),
                new Class<?>[]{HttpServletResponse.class},
                (proxy, method, args) -> null
        );

        servlet.doPost(req, resp);
        assertEquals("Invalid OTP code. Please check and try again.", reqAttrs.get("error"));
        assertEquals("/admin-otp-verify.jsp", forwardedPath.get());
    }
}
