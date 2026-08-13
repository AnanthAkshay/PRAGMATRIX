<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="OTP Verification — PRAGMATRIX 2026. Enter the OTP sent to your email.">
    <title>Verify OTP — PRAGMATRIX 2026</title>
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
                <a href="${pageContext.request.contextPath}/team-login">Back</a>
            </div>
        </nav>
    </header>

    <!-- OTP Verify Form -->
    <div class="centered-form-wrapper">
        <div class="centered-form-card glass-panel">
            <div class="form-card-header">
                <img src="${pageContext.request.contextPath}/images/archway-bg.jpeg" alt="PRAGMATRIX 2026" class="form-logo">
                <h2>Verify OTP</h2>
                <p>An OTP has been sent to <strong><c:out value="${maskedEmail}"/></strong></p>
            </div>

            <!-- Success alert -->
            <c:if test="${not empty success}">
                <div class="alert alert-success" id="success-alert">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>
                    <c:out value="${success}"/>
                </div>
            </c:if>

            <!-- Error alert -->
            <c:if test="${not empty error}">
                <div class="alert alert-error" id="error-alert">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
                    <c:out value="${error}"/>
                </div>
            </c:if>

            <form action="${pageContext.request.contextPath}/team-otp-verify" method="POST" id="otp-form" novalidate>

                <!-- OTP Input -->
                <div class="form-group">
                    <label for="otp" class="form-label">One-Time Password <span class="required">*</span></label>
                    <input type="text" name="otp" id="otp" class="form-control"
                           placeholder="Enter 6-digit OTP"
                           required maxlength="6" pattern="[0-9]{6}"
                           inputmode="numeric" autocomplete="one-time-code"
                           style="font-family: var(--font-display); font-size: 1.5rem; letter-spacing: 8px; text-align: center;">
                    <p class="form-hint">Valid for 5 minutes</p>
                </div>

                <button type="submit" class="btn btn-primary btn-lg w-100" id="btn-verify-otp">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
                        <polyline points="22 4 12 14.01 9 11.01"/>
                    </svg>
                    Verify &amp; Login
                </button>
            </form>

            <!-- Resend OTP -->
            <div class="form-footer">
                <form action="${pageContext.request.contextPath}/team-otp-verify" method="POST" style="display:inline;">
                    <input type="hidden" name="action" value="resend">
                    <button type="submit" class="btn btn-outline btn-sm" id="btn-resend-otp" style="margin-bottom: 0.75rem;">
                        Resend OTP
                    </button>
                </form>
                <br>
                <a href="${pageContext.request.contextPath}/team-login">&larr; Use a different Team Code</a>
            </div>
        </div>
    </div>

    <!-- Client-side: countdown for resend button -->
    <script>
    (function() {
        var resendBtn = document.getElementById('btn-resend-otp');
        var cooldownSeconds = 60;
        var storageKey = 'otpResendCooldown';

        // Check if we have an active cooldown
        var lastSent = localStorage.getItem(storageKey);
        if (lastSent) {
            var elapsed = Math.floor((Date.now() - parseInt(lastSent)) / 1000);
            if (elapsed < cooldownSeconds) {
                startCountdown(cooldownSeconds - elapsed);
            }
        }

        resendBtn.addEventListener('click', function() {
            localStorage.setItem(storageKey, Date.now().toString());
        });

        function startCountdown(seconds) {
            resendBtn.disabled = true;
            var remaining = seconds;
            var originalText = resendBtn.textContent;
            var interval = setInterval(function() {
                resendBtn.textContent = 'Resend OTP (' + remaining + 's)';
                remaining--;
                if (remaining < 0) {
                    clearInterval(interval);
                    resendBtn.textContent = originalText;
                    resendBtn.disabled = false;
                }
            }, 1000);
        }

        // Auto-focus OTP input
        document.getElementById('otp').focus();

        // Auto-dismiss alerts
        setTimeout(function() {
            var alerts = document.querySelectorAll('.alert');
            alerts.forEach(function(a) {
                a.style.transition = 'opacity 0.5s ease';
                a.style.opacity = '0';
                setTimeout(function() { a.remove(); }, 500);
            });
        }, 5000);
    })();
    </script>

</body>
</html>
