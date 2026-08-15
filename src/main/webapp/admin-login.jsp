<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="Admin login for PRAGMATRIX 2026 quiz management system.">
    <title>Admin Login — PRAGMATRIX 2026</title>
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
                <a href="${pageContext.request.contextPath}/register">Register Team</a>
            </div>
        </nav>
    </header>

    <!-- Login Form -->
    <div class="centered-form-wrapper">
        <div class="centered-form-card glass-panel">
            <div class="form-card-header">
                <img src="${pageContext.request.contextPath}/images/archway-bg.jpeg" alt="PRAGMATRIX 2026" class="form-logo">
                <h2>Admin Portal</h2>
                <p>Sign in to manage quizzes, rounds, and scores</p>
            </div>

            <!-- Error alert -->
            <c:if test="${not empty error}">
                <div class="alert alert-error" id="login-error">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
                    <c:out value="${error}"/>
                </div>
            </c:if>

            <form action="${pageContext.request.contextPath}/login" method="POST" id="login-form" novalidate>
                <div class="form-group">
                    <label for="username" class="form-label">Username / Email <span class="required">*</span></label>
                    <input type="text" name="username" id="username" class="form-control"
                           placeholder="admin1 or email@example.com"
                           value="<c:out value='${email}'/>"
                           required autocomplete="username">
                </div>

                <div class="form-group">
                    <label for="password" class="form-label">Password <span class="required">*</span></label>
                    <input type="password" name="password" id="password" class="form-control"
                           placeholder="Enter your password"
                           required autocomplete="current-password">
                </div>

                <button type="submit" class="btn btn-primary btn-lg w-100" id="btn-login">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <path d="M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4"/>
                        <polyline points="10 17 15 12 10 7"/>
                        <line x1="15" y1="12" x2="3" y2="12"/>
                    </svg>
                    Sign In
                </button>
            </form>

            <div class="form-footer">
                <a href="${pageContext.request.contextPath}/">&larr; Back to Home</a>
            </div>
        </div>
    </div>

    <script>
    document.getElementById('login-form').addEventListener('submit', function(e) {
        var email = document.getElementById('email').value.trim();
        var password = document.getElementById('password').value;
        if (!email || !password) {
            e.preventDefault();
            var alertDiv = document.getElementById('login-error');
            if (!alertDiv) {
                alertDiv = document.createElement('div');
                alertDiv.id = 'login-error';
                alertDiv.className = 'alert alert-error';
                document.querySelector('.form-card-header').insertAdjacentElement('afterend', alertDiv);
            }
            alertDiv.innerHTML = '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg> Email and password are required.';
        }
    });
    </script>

</body>
</html>
