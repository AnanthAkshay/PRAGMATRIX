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
                <a href="${pageContext.request.contextPath}/register">Register</a>
            </div>
        </nav>
    </header>

    <!-- Login Form -->
    <div class="centered-form-wrapper">
        <div class="centered-form-card glass-panel">
            <div class="form-card-header">
                <img src="${pageContext.request.contextPath}/images/archway-bg.jpeg" alt="PRAGMATRIX 2026" class="form-logo">
                <h2>Admin Portal</h2>
                <p>Sign in to manage quizzes and scores</p>
            </div>

            <!-- Error alert -->
            <c:if test="${not empty error}">
                <div class="alert alert-error" id="login-error">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
                    <c:out value="${error}"/>
                </div>
            </c:if>

            <div id="password-login-section">
                <form action="${pageContext.request.contextPath}/login" method="POST" id="login-form" novalidate>
                    <input type="hidden" name="loginType" value="password">
                    <div class="form-group">
                        <label for="username" class="form-label">Username</label>
                        <input type="text" name="username" id="username" class="form-control"
                               placeholder="Enter your admin username"
                               value="<c:out value='${username}'/>"
                               required autocomplete="username">
                    </div>

                    <div class="form-group">
                        <label for="password" class="form-label">Password</label>
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
                    
                    <div style="text-align: center; margin-top: 15px;">
                        <a href="#" onclick="toggleLoginMethod('otp')">Login with OTP instead</a>
                    </div>
                </form>
            </div>
            
            <div id="otp-login-section" style="display: none;">
                <form action="${pageContext.request.contextPath}/login" method="POST" id="otp-request-form" novalidate>
                    <input type="hidden" name="loginType" value="otp_request">
                    <div class="form-group">
                        <label for="otp_username" class="form-label">Username</label>
                        <input type="text" name="username" id="otp_username" class="form-control"
                               placeholder="Enter your admin username"
                               value="<c:out value='${username}'/>"
                               required>
                    </div>

                    <div class="form-group">
                        <label for="email" class="form-label">Email Address</label>
                        <input type="email" name="email" id="email" class="form-control"
                               placeholder="Enter your registered email"
                               required>
                    </div>

                    <button type="submit" class="btn btn-primary btn-lg w-100" id="btn-otp-request">
                        Request OTP
                    </button>
                    
                    <div style="text-align: center; margin-top: 15px;">
                        <a href="#" onclick="toggleLoginMethod('password')">Login with Password instead</a>
                    </div>
                </form>
            </div>

            <div class="form-footer">
                <a href="${pageContext.request.contextPath}/">&larr; Back to Home</a>
            </div>
        </div>
    </div>

    <script>
    function toggleLoginMethod(method) {
        if (method === 'otp') {
            document.getElementById('password-login-section').style.display = 'none';
            document.getElementById('otp-login-section').style.display = 'block';
        } else {
            document.getElementById('password-login-section').style.display = 'block';
            document.getElementById('otp-login-section').style.display = 'none';
        }
    }
    
    document.getElementById('login-form').addEventListener('submit', function(e) {
        var username = document.getElementById('username').value.trim();
        var password = document.getElementById('password').value;
        if (!username || !password) {
            e.preventDefault();
            showError('Username and password are required.');
        }
    });
    
    document.getElementById('otp-request-form').addEventListener('submit', function(e) {
        var username = document.getElementById('otp_username').value.trim();
        var email = document.getElementById('email').value.trim();
        if (!username || !email) {
            e.preventDefault();
            showError('Username and email are required.');
        }
    });
    
    function showError(msg) {
        var alertDiv = document.getElementById('login-error');
        if (!alertDiv) {
            alertDiv = document.createElement('div');
            alertDiv.id = 'login-error';
            alertDiv.className = 'alert alert-error';
            document.querySelector('.form-card-header').insertAdjacentElement('afterend', alertDiv);
        }
        alertDiv.innerHTML = '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg> ' + msg;
    }
    </script>

</body>
</html>
