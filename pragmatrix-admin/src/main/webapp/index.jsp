<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%
    // If already logged in as admin, redirect directly to dashboard
    HttpSession sess = request.getSession(false);
    if (sess != null && sess.getAttribute("adminId") != null) {
        response.sendRedirect(request.getContextPath() + "/admin/dashboard");
        return;
    }
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="PRAGMATRIX 2026 Admin Portal — Official management and scoring system for BizWizX and Vortex events.">
    <title>PRAGMATRIX 2026 — Admin Portal</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/theme.css">
</head>
<body class="admin-landing-body">

    <!-- ===== ADMIN LANDING HERO SECTION ===== -->
    <main class="admin-landing-container">
        <!-- Top Breathing Room & Official Institutional Branding -->
        <div class="admin-landing-branding">
            <jsp:include page="/includes/header-branding.jsp" />
        </div>

        <!-- Dedicated Admin Login CTA Action Button -->
        <div class="admin-cta-wrapper">
            <a href="${pageContext.request.contextPath}/login" class="btn btn-admin-login" id="btn-admin-login">
                <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
                    <rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect>
                    <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
                </svg>
                <span>ADMIN LOGIN</span>
            </a>
        </div>
    </main>

    <!-- ===== FOOTER ===== -->
    <footer class="site-footer">
        <span class="footer-brand">PRAGMATRIX 2026</span> &mdash; Admin Portal &bull; All rights reserved.
    </footer>

</body>
</html>
