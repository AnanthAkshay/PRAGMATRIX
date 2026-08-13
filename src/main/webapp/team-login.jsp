<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="Team Login — PRAGMATRIX 2026. Enter your Team Code to access your dashboard.">
    <title>Team Login — PRAGMATRIX 2026</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/theme.css">
</head>
<body>

    <!-- Header -->
    <header class="site-header">
        <nav class="header-nav">
            <a href="${pageContext.request.contextPath}/" class="header-brand">
                <img src="${pageContext.request.contextPath}/images/archway-bg.jpeg" alt="PRAGMATRIX" class="brand-logo">
                <span class="brand-text">Pragmatrix 2026</span>
            </a>
            <div class="header-links">
                <a href="${pageContext.request.contextPath}/">Home</a>
                <a href="${pageContext.request.contextPath}/login">Admin</a>
            </div>
        </nav>
    </header>

    <!-- Team Login Form -->
    <div class="centered-form-wrapper">
        <div class="centered-form-card glass-panel">
            <div class="form-card-header">
                <img src="${pageContext.request.contextPath}/images/archway-bg.jpeg" alt="PRAGMATRIX 2026" class="form-logo">
                <h2>Team Login</h2>
                <p>Enter your Team Code to access your dashboard</p>
            </div>

            <!-- Error alert -->
            <c:if test="${not empty error}">
                <div class="alert alert-error" id="error-alert">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
                    <c:out value="${error}"/>
                </div>
            </c:if>

            <form action="${pageContext.request.contextPath}/team-login" method="POST" id="team-login-form" novalidate>

                <!-- Team Code -->
                <div class="form-group">
                    <label for="teamCode" class="form-label">Team Code <span class="required">*</span></label>
                    <input type="text" name="teamCode" id="teamCode" class="form-control"
                           placeholder="e.g. PMBZ047"
                           value="<c:out value='${teamCode}'/>"
                           required maxlength="15"
                           style="text-transform: uppercase; font-family: var(--font-display); font-size: 1.1rem; letter-spacing: 2px; text-align: center;">
                    <p class="form-hint">Enter the unique Participant ID you received via email</p>
                </div>

                <button type="submit" class="btn btn-primary btn-lg w-100" id="btn-send-otp">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/>
                        <polyline points="22,6 12,13 2,6"/>
                    </svg>
                    Send OTP
                </button>
            </form>

            <div class="form-footer">
                <a href="${pageContext.request.contextPath}/">&larr; Back to Home</a>
            </div>
        </div>
    </div>

    <!-- Client-side validation -->
    <script>
    document.getElementById('team-login-form').addEventListener('submit', function(e) {
        var code = document.getElementById('teamCode').value.trim();
        if (!code) {
            e.preventDefault();
            var alertDiv = document.getElementById('error-alert');
            if (!alertDiv) {
                alertDiv = document.createElement('div');
                alertDiv.id = 'error-alert';
                alertDiv.className = 'alert alert-error';
                this.parentNode.insertBefore(alertDiv, this);
            }
            alertDiv.innerHTML = '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg> Please enter your Team Code.';
        }
    });
    </script>

</body>
</html>
