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
                <img src="${pageContext.request.contextPath}/images/set-logo.jpg" alt="Seshadripuram Educational Trust" class="brand-logo" style="border-radius: 50%;">
                <span class="brand-text">Pragmatrix 2026</span>
            </a>
            <div class="header-links">
                <a href="${pageContext.request.contextPath}/">Home</a>
            </div>
        </nav>
    </header>

    <!-- Team Login Form / Locked Container -->
    <div class="centered-form-wrapper">
        <div class="centered-form-card glass-panel" style="max-width: 520px; width: 100%;">
            <div class="form-card-header">
                <img src="${pageContext.request.contextPath}/images/pragmatrix-crest.png" alt="PRAGMATRIX 2026" class="form-logo" style="height: 90px; width: auto;">
                
                <c:choose>
                    <c:when test="${isLocked}">
                        <div style="margin: 0.75rem 0 0.25rem 0;">
                            <span class="badge" style="background: rgba(212, 175, 55, 0.15); color: var(--gold-600); border: 1px solid var(--gold-600); font-size: 0.85rem; padding: 0.35rem 0.8rem; border-radius: var(--radius-full); font-weight: 700; display: inline-flex; align-items: center; gap: 0.4rem;">
                                <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>
                                TEAM LOGIN LOCKED
                            </span>
                        </div>
                        <h2 style="margin-top: 0.5rem; font-size: 1.5rem;">Opens August 24 @ 9:00 AM IST</h2>
                        <p style="color: var(--gray-600); margin-top: 0.5rem; font-size: 0.95rem; line-height: 1.5;">
                            Team Login opens on <strong>August 24, 2026 at 9:00 AM IST</strong>. Please check back then to access your team dashboard.
                        </p>
                    </c:when>
                    <c:otherwise>
                        <h2>Team Login</h2>
                        <p>Enter your Team Code to access your dashboard</p>
                    </c:otherwise>
                </c:choose>
            </div>

            <!-- Error alert -->
            <c:if test="${not empty error}">
                <div class="alert alert-error" id="error-alert" style="margin-top: 1rem;">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
                    <c:out value="${error}"/>
                </div>
            </c:if>

            <c:choose>
                <c:when test="${isLocked}">
                    <!-- Live Countdown Display on Locked Login Page -->
                    <div id="login-countdown-banner" class="countdown-banner" style="margin: 1.25rem 0 0.5rem 0; width: 100%; box-sizing: border-box; padding: 1rem 0.5rem;">
                        <div class="countdown-banner-title" style="font-size: 0.8rem; margin-bottom: 0.5rem;">
                            <span class="live-dot"></span>
                            TIME REMAINING UNTIL TEAM LOGIN UNLOCKS
                        </div>
                        <div class="countdown-grid" style="gap: 0.4rem; justify-content: center;">
                            <div class="countdown-box" style="min-width: 50px; padding: 0.3rem 0.4rem;">
                                <div class="countdown-number" id="login-countdown-days" style="font-size: 1.2rem;">00</div>
                                <div class="countdown-label" style="font-size: 0.6rem;">Days</div>
                            </div>
                            <div class="countdown-colon" style="font-size: 1.1rem; align-self: center;">:</div>
                            <div class="countdown-box" style="min-width: 50px; padding: 0.3rem 0.4rem;">
                                <div class="countdown-number" id="login-countdown-hours" style="font-size: 1.2rem;">00</div>
                                <div class="countdown-label" style="font-size: 0.6rem;">Hours</div>
                            </div>
                            <div class="countdown-colon" style="font-size: 1.1rem; align-self: center;">:</div>
                            <div class="countdown-box" style="min-width: 50px; padding: 0.3rem 0.4rem;">
                                <div class="countdown-number" id="login-countdown-minutes" style="font-size: 1.2rem;">00</div>
                                <div class="countdown-label" style="font-size: 0.6rem;">Mins</div>
                            </div>
                            <div class="countdown-colon" style="font-size: 1.1rem; align-self: center;">:</div>
                            <div class="countdown-box" style="min-width: 50px; padding: 0.3rem 0.4rem;">
                                <div class="countdown-number" id="login-countdown-seconds" style="font-size: 1.2rem;">00</div>
                                <div class="countdown-label" style="font-size: 0.6rem;">Secs</div>
                            </div>
                        </div>
                    </div>
                </c:when>

                <c:otherwise>
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
                </c:otherwise>
            </c:choose>

            <div class="form-footer" style="margin-top: 1.25rem;">
                <a href="${pageContext.request.contextPath}/">&larr; Back to Home</a>
            </div>
        </div>
    </div>

    <!-- Client-side script -->
    <script>
    <c:choose>
        <c:when test="${isLocked}">
        (function() {
            // Target: August 24, 2026 9:00 AM IST (UTC+5:30) = 03:30:00 UTC on Aug 24, 2026
            const TARGET_TIME = Date.UTC(2026, 7, 24, 3, 30, 0);
            const elDays = document.getElementById('login-countdown-days');
            const elHours = document.getElementById('login-countdown-hours');
            const elMinutes = document.getElementById('login-countdown-minutes');
            const elSeconds = document.getElementById('login-countdown-seconds');

            function updateCountdown() {
                const now = Date.now();
                const diff = TARGET_TIME - now;
                if (diff <= 0) {
                    window.location.reload();
                    return false;
                }
                const totalSec = Math.floor(diff / 1000);
                const days = Math.floor(totalSec / 86400);
                const hours = Math.floor((totalSec % 86400) / 3600);
                const minutes = Math.floor((totalSec % 3600) / 60);
                const seconds = totalSec % 60;

                if (elDays) elDays.textContent = String(days).padStart(2, '0');
                if (elHours) elHours.textContent = String(hours).padStart(2, '0');
                if (elMinutes) elMinutes.textContent = String(minutes).padStart(2, '0');
                if (elSeconds) elSeconds.textContent = String(seconds).padStart(2, '0');
                return true;
            }

            if (updateCountdown()) {
                setInterval(updateCountdown, 1000);
            }
        })();
        </c:when>
        <c:otherwise>
        var loginForm = document.getElementById('team-login-form');
        if (loginForm) {
            loginForm.addEventListener('submit', function(e) {
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
        }
        </c:otherwise>
    </c:choose>
    </script>

</body>
</html>
