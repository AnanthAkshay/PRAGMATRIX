package com.pragmatrix.servlet;

import com.pragmatrix.dao.AdminDAO;
import com.pragmatrix.model.Admin;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Handles admin OTP verification.
 */
@WebServlet(name = "AdminOtpVerifyServlet", urlPatterns = {"/admin-otp-verify"})
public class AdminOtpVerifyServlet extends HttpServlet {

    private final AdminDAO adminDAO = new AdminDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("pendingAdminUsername") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        req.getRequestDispatcher("/admin-otp-verify.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("pendingAdminUsername") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String expectedOtp = (String) session.getAttribute("adminLoginOtp");
        String username = (String) session.getAttribute("pendingAdminUsername");
        Long sentAt = (Long) session.getAttribute("adminOtpLastSentAt");
        
        String inputOtp = req.getParameter("otp");

        // Validate expiry (5 mins)
        if (sentAt == null || System.currentTimeMillis() - sentAt > 5 * 60 * 1000) {
            req.setAttribute("error", "OTP has expired. Please request a new one.");
            session.removeAttribute("adminLoginOtp");
            req.getRequestDispatcher("/admin-otp-verify.jsp").forward(req, resp);
            return;
        }

        if (inputOtp == null || !inputOtp.trim().equals(expectedOtp)) {
            req.setAttribute("error", "Invalid OTP.");
            req.getRequestDispatcher("/admin-otp-verify.jsp").forward(req, resp);
            return;
        }

        try {
            Admin admin = adminDAO.findByUsername(username);
            if (admin != null) {
                // Clear pending auth
                session.removeAttribute("pendingAdminUsername");
                session.removeAttribute("adminLoginOtp");
                session.removeAttribute("adminOtpLastSentAt");
                
                // Login
                session.setAttribute("adminId", admin.getAdminId());
                session.setAttribute("adminName", admin.getFullName());
                session.setAttribute("adminUsername", admin.getUsername());
                session.setMaxInactiveInterval(30 * 60);

                resp.sendRedirect(req.getContextPath() + "/admin/dashboard");
            } else {
                resp.sendRedirect(req.getContextPath() + "/login");
            }
        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("error", "Error verifying OTP.");
            req.getRequestDispatcher("/admin-otp-verify.jsp").forward(req, resp);
        }
    }
}
