package com.pragmatrix.servlet;

import com.pragmatrix.dao.AdminDAO;
import com.pragmatrix.model.Admin;
import com.pragmatrix.util.PasswordUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import com.pragmatrix.util.EmailService;
import com.pragmatrix.util.OtpUtil;

/**
 * Handles admin login.
 * GET  /login → display login form
 * POST /login → authenticate, create session, redirect to dashboard
 */
@WebServlet(name = "AdminLoginServlet", urlPatterns = {"/login"})
public class AdminLoginServlet extends HttpServlet {

    private final AdminDAO adminDAO = new AdminDAO();

    private boolean isAuthorizedAdminEmail(String email) {
        if (email == null) return false;
        String e = email.trim().toLowerCase();
        return "svs262003@gmail.com".equals(e) || "shirishvshandilya@gmail.com".equals(e);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // If already logged in, redirect to dashboard
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("adminId") != null) {
            resp.sendRedirect(req.getContextPath() + "/admin/dashboard");
            return;
        }
        req.getRequestDispatcher("/admin-login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        String loginType = req.getParameter("loginType");
        
        if ("otp_request".equals(loginType)) {
            String email = req.getParameter("email");
            
            if (email == null || email.trim().isEmpty()) {
                req.setAttribute("error", "Email is required for OTP login.");
                req.getRequestDispatcher("/admin-login.jsp").forward(req, resp);
                return;
            }
            
            if (!isAuthorizedAdminEmail(email)) {
                req.setAttribute("error", "Access denied. Unauthorized admin email address.");
                req.setAttribute("email", email);
                req.getRequestDispatcher("/admin-login.jsp").forward(req, resp);
                return;
            }
            
            try {
                Admin admin = adminDAO.findByEmail(email.trim());
                if (admin == null) {
                    req.setAttribute("error", "Email does not match our records.");
                    req.setAttribute("email", email);
                    req.getRequestDispatcher("/admin-login.jsp").forward(req, resp);
                    return;
                }
                
                String otpCode = OtpUtil.generateOtp();
                boolean emailSent = EmailService.sendOtpEmail(admin.getEmail(), otpCode);
                
                HttpSession session = req.getSession(true);
                session.setAttribute("pendingAdminUsername", admin.getUsername());
                session.setAttribute("adminLoginOtp", otpCode);
                session.setAttribute("adminOtpLastSentAt", System.currentTimeMillis());
                
                resp.sendRedirect(req.getContextPath() + "/admin-otp-verify");
                return;
                
            } catch (Exception e) {
                e.printStackTrace();
                req.setAttribute("error", "An error occurred generating OTP.");
                req.getRequestDispatcher("/admin-login.jsp").forward(req, resp);
                return;
            }
        }

        String email = req.getParameter("email");
        String password = req.getParameter("password");

        if (email == null || email.trim().isEmpty() ||
            password == null || password.trim().isEmpty()) {
            req.setAttribute("error", "Email and password are required.");
            req.getRequestDispatcher("/admin-login.jsp").forward(req, resp);
            return;
        }

        if (!isAuthorizedAdminEmail(email)) {
            req.setAttribute("error", "Access denied. Unauthorized admin email address.");
            req.setAttribute("email", email);
            req.getRequestDispatcher("/admin-login.jsp").forward(req, resp);
            return;
        }

        try {
            Admin admin = adminDAO.findByEmail(email.trim());

            if (admin == null || !PasswordUtil.checkPassword(password, admin.getPasswordHash())) {
                req.setAttribute("error", "Invalid email or password.");
                req.setAttribute("email", email);
                req.getRequestDispatcher("/admin-login.jsp").forward(req, resp);
                return;
            }

            // Create session
            HttpSession session = req.getSession(true);
            session.setAttribute("adminId", admin.getAdminId());
            session.setAttribute("adminName", admin.getFullName());
            session.setAttribute("adminUsername", admin.getUsername());
            session.setMaxInactiveInterval(30 * 60); // 30 minutes

            resp.sendRedirect(req.getContextPath() + "/admin/dashboard");

        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("error", "Login failed. Please try again.");
            req.getRequestDispatcher("/admin-login.jsp").forward(req, resp);
        }
    }
}
