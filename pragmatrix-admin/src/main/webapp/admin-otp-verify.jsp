<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="Admin OTP Verification — PRAGMATRIX 2026.">
    <title>Verify Admin OTP — PRAGMATRIX 2026</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/theme.css">
</head>
<body>

    <!-- Full Header Branding Block -->
    <div style="padding-top: 1rem;">
        <jsp:include page="/includes/header-branding.jsp" />
    </div>

    <!-- OTP Verify Form -->
    <div class="centered-form-wrapper">
        <div class="centered-form-card glass-panel">
            <div class="form-card-header">
                <img src="${pageContext.request.contextPath}/images/pragmatrix-crest.png" alt="PRAGMATRIX 2026" class="form-logo" style="height: 90px; width: auto;">
                <h2>Verify Admin OTP</h2>
                <p>An OTP has been sent to <strong><c:out value="${sessionScope.admin_pending_email}"/></strong></p>
            </div>

            <!-- Success message alert -->
            <c:if test="${not empty message}">
                <div class="alert alert-success" id="info-alert">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>
                    <c:out value="${message}"/>
                </div>
            </c:if>

            <!-- Error alert -->
            <c:if test="${not empty error}">
                <div class="alert alert-error" id="error-alert">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
                    <c:out value="${error}"/>
                </div>
            </c:if>

            <form action="${pageContext.request.contextPath}/admin-otp-verify" method="POST" id="otp-form" novalidate>
                <!-- OTP Input -->
                <div class="form-group">
                    <label for="otp" class="form-label">6-Digit One-Time Password <span class="required">*</span></label>
                    <input type="text" name="otp" id="otp" class="form-control"
                           placeholder="──────"
                           required maxlength="6" pattern="[0-9]{6}"
                           inputmode="numeric" autocomplete="one-time-code"
                           style="font-family: var(--font-display); font-size: 1.6rem; letter-spacing: 10px; text-align: center;">
                </div>

                <button type="submit" class="btn btn-primary btn-lg w-100" id="btn-verify-otp">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
                        <polyline points="22 4 12 14.01 9 11.01"/>
                    </svg>
                    Verify &amp; Sign In
                </button>
            </form>

            <div style="margin-top: 1rem; text-align: center;">
                <form action="${pageContext.request.contextPath}/admin-otp-verify" method="POST" style="display: inline;">
                    <input type="hidden" name="action" value="resend">
                    <button type="submit" class="btn btn-sm btn-outline" style="color: var(--gold-700); border-color: var(--gold-600);">
                        Resend OTP
                    </button>
                </form>
            </div>

            <div class="form-footer">
                <a href="${pageContext.request.contextPath}/login">&larr; Return to Admin Login</a>
            </div>
        </div>
    </div>
</body>
</html>
