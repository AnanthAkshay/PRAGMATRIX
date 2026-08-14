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
                           placeholder="e.g. PMBZ001"
                           value="<c:out value='${teamCode}'/>"
                           required maxlength="15"
                           style="text-transform: uppercase; font-family: var(--font-display); font-size: 1.1rem; letter-spacing: 2px; text-align: center;">
                    <p class="form-hint">Enter the Team Code you received in your registration confirmation email</p>
                </div>

                <button type="submit" class="btn btn-primary btn-lg w-100" id="btn-team-login">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <path d="M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4"/>
                        <polyline points="10 17 15 12 10 7"/>
                        <line x1="15" y1="12" x2="3" y2="12"/>
                    </svg>
                    Access Dashboard
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
