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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Admin Account Management.
 * Accessible only to logged-in admins (guarded by AdminAuthFilter).
 *
 * GET  /admin/admins → list all admin accounts and display Add Admin form
 * POST /admin/admins → add new admin (cap 10) or delete an existing admin
 */
@WebServlet(name = "AdminManageServlet", urlPatterns = {"/admin/admins"})
public class AdminManageServlet extends HttpServlet {

    private final AdminDAO adminDAO = new AdminDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            List<Admin> admins = adminDAO.findAll();
            int adminCount = admins.size();

            req.setAttribute("admins", admins);
            req.setAttribute("adminCount", adminCount);
            req.setAttribute("maxAdmins", AdminDAO.MAX_ADMIN_CAP);

            req.getRequestDispatcher("/WEB-INF/views/admin-manage.jsp").forward(req, resp);

        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("error", "Failed to load admin accounts: " + e.getMessage());
            req.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession(false);
        Integer currentAdminId = (session != null) ? (Integer) session.getAttribute("adminId") : null;

        String action = req.getParameter("action");

        try {
            if ("delete".equalsIgnoreCase(action)) {
                handleDeleteAdmin(req, resp, currentAdminId);
            } else {
                handleAddAdmin(req, resp);
            }
        } catch (Exception e) {
            e.printStackTrace();
            String errorMsg = URLEncoder.encode("Operation failed: " + e.getMessage(), StandardCharsets.UTF_8);
            resp.sendRedirect(req.getContextPath() + "/admin/admins?error=" + errorMsg);
        }
    }

    private void handleAddAdmin(HttpServletRequest req, HttpServletResponse resp)
            throws Exception {
        String fullName = req.getParameter("fullName");
        String email = req.getParameter("email");
        String password = req.getParameter("password");

        if (fullName != null) fullName = fullName.trim();
        if (email != null) email = email.trim();
        if (password != null) password = password.trim();

        if (fullName == null || fullName.isEmpty() ||
            email == null || email.isEmpty() ||
            password == null || password.isEmpty()) {
            String error = URLEncoder.encode("All fields (Name, Email, Password) are required.", StandardCharsets.UTF_8);
            resp.sendRedirect(req.getContextPath() + "/admin/admins?error=" + error);
            return;
        }

        if (!email.contains("@") || !email.contains(".")) {
            String error = URLEncoder.encode("Please enter a valid email address.", StandardCharsets.UTF_8);
            resp.sendRedirect(req.getContextPath() + "/admin/admins?error=" + error);
            return;
        }

        if (password.length() < 6) {
            String error = URLEncoder.encode("Password must be at least 6 characters.", StandardCharsets.UTF_8);
            resp.sendRedirect(req.getContextPath() + "/admin/admins?error=" + error);
            return;
        }

        // Check 10 admin cap
        int currentCount = adminDAO.count();
        if (currentCount >= AdminDAO.MAX_ADMIN_CAP) {
            String error = URLEncoder.encode("Maximum of 10 admins reached. Remove an existing admin before adding a new one.", StandardCharsets.UTF_8);
            resp.sendRedirect(req.getContextPath() + "/admin/admins?error=" + error);
            return;
        }

        // Check duplicate email
        if (adminDAO.existsByEmail(email)) {
            String error = URLEncoder.encode("An admin account with this email already exists.", StandardCharsets.UTF_8);
            resp.sendRedirect(req.getContextPath() + "/admin/admins?error=" + error);
            return;
        }

        // Hash password with bcrypt
        String hashedPassword = PasswordUtil.hashPassword(password);

        Admin newAdmin = new Admin(0, email, hashedPassword, fullName, email);
        boolean inserted = adminDAO.insert(newAdmin);

        if (inserted) {
            String success = URLEncoder.encode("Admin account for '" + fullName + "' (" + email + ") added successfully.", StandardCharsets.UTF_8);
            resp.sendRedirect(req.getContextPath() + "/admin/admins?success=" + success);
        } else {
            String error = URLEncoder.encode("Failed to create admin account. System cap may have been reached.", StandardCharsets.UTF_8);
            resp.sendRedirect(req.getContextPath() + "/admin/admins?error=" + error);
        }
    }

    private void handleDeleteAdmin(HttpServletRequest req, HttpServletResponse resp, Integer currentAdminId)
            throws Exception {
        String adminIdStr = req.getParameter("adminId");
        if (adminIdStr == null || adminIdStr.trim().isEmpty()) {
            String error = URLEncoder.encode("Invalid admin ID specified.", StandardCharsets.UTF_8);
            resp.sendRedirect(req.getContextPath() + "/admin/admins?error=" + error);
            return;
        }

        int targetAdminId = Integer.parseInt(adminIdStr.trim());

        // Check if admin is trying to delete their own account
        if (currentAdminId != null && currentAdminId == targetAdminId) {
            String error = URLEncoder.encode("You cannot delete your own admin account while logged in.", StandardCharsets.UTF_8);
            resp.sendRedirect(req.getContextPath() + "/admin/admins?error=" + error);
            return;
        }

        // Check if this is the last remaining admin
        int count = adminDAO.count();
        if (count <= 1) {
            String error = URLEncoder.encode("Cannot delete the last remaining admin account in the system.", StandardCharsets.UTF_8);
            resp.sendRedirect(req.getContextPath() + "/admin/admins?error=" + error);
            return;
        }

        Admin targetAdmin = adminDAO.findById(targetAdminId);
        if (targetAdmin == null) {
            String error = URLEncoder.encode("Admin account not found.", StandardCharsets.UTF_8);
            resp.sendRedirect(req.getContextPath() + "/admin/admins?error=" + error);
            return;
        }

        boolean deleted = adminDAO.delete(targetAdminId);
        if (deleted) {
            String success = URLEncoder.encode("Admin account for '" + targetAdmin.getFullName() + "' (" + targetAdmin.getEmail() + ") removed.", StandardCharsets.UTF_8);
            resp.sendRedirect(req.getContextPath() + "/admin/admins?success=" + success);
        } else {
            String error = URLEncoder.encode("Failed to remove admin account.", StandardCharsets.UTF_8);
            resp.sendRedirect(req.getContextPath() + "/admin/admins?error=" + error);
        }
    }
}
