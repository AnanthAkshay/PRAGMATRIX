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

    <!-- Full Header Branding Block -->
    <div style="padding-top: 1rem;">
        <jsp:include page="/includes/header-branding.jsp" />
    </div>

    <!-- Login Form -->
    <div class="centered-form-wrapper">
        <div class="centered-form-card glass-panel">
            <div class="form-card-header">
                <img src="${pageContext.request.contextPath}/images/pragmatrix-crest.png" alt="PRAGMATRIX 2026" class="form-logo" style="height: 90px; width: auto;">
                <h2>Admin Portal</h2>
                <p>Enter your authorized email to receive a 6-digit OTP</p>
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
                    <label for="email" class="form-label">Authorized Admin Email <span class="required">*</span></label>
                    <input type="email" name="email" id="email" class="form-control"
                           placeholder="svs262003@gmail.com or ananthakshay2006@gmail.com"
                           value="<c:out value='${email}'/>"
                           required autocomplete="email">
                </div>

                <button type="submit" class="btn btn-primary btn-lg w-100" id="btn-login">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <path d="M22 2L11 13"/>
                        <polygon points="22 2 15 22 11 13 2 9 22 2"/>
                    </svg>
                    Send OTP
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
        if (!email) {
            e.preventDefault();
            var alertDiv = document.getElementById('login-error');
            if (!alertDiv) {
                alertDiv = document.createElement('div');
                alertDiv.id = 'login-error';
                alertDiv.className = 'alert alert-error';
                document.querySelector('.form-card-header').insertAdjacentElement('afterend', alertDiv);
            }
            alertDiv.innerHTML = '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg> Email address is required.';
        }
    });
    </script>

</body>
</html>
