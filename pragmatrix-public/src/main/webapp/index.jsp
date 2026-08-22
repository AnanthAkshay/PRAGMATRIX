<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="PRAGMATRIX 2026 — Applied Management Carnival. Inter-college quiz scoring, live leaderboards, and team registration for BizWizX and Vortex events.">
    <title>PRAGMATRIX 2026 — Applied Management Carnival</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/theme.css">
</head>
<body>

    <!-- ===== HERO SECTION ===== -->
    <section class="hero-landing">
        <jsp:include page="/includes/header-branding.jsp" />

        <!-- ===== COUNTDOWN BANNER ===== -->
        <div id="live-countdown-banner" class="countdown-banner" style="display: none;">
            <div class="countdown-banner-title">
                <span class="live-dot"></span>
                PRAGMATRIX 2026 goes live on August 24, 2026 at 9:00 AM
            </div>
            <div class="countdown-grid">
                <div class="countdown-box">
                    <div class="countdown-number" id="countdown-days">00</div>
                    <div class="countdown-label">Days</div>
                </div>
                <div class="countdown-colon">:</div>
                <div class="countdown-box">
                    <div class="countdown-number" id="countdown-hours">00</div>
                    <div class="countdown-label">Hours</div>
                </div>
                <div class="countdown-colon">:</div>
                <div class="countdown-box">
                    <div class="countdown-number" id="countdown-minutes">00</div>
                    <div class="countdown-label">Mins</div>
                </div>
                <div class="countdown-colon">:</div>
                <div class="countdown-box">
                    <div class="countdown-number" id="countdown-seconds">00</div>
                    <div class="countdown-label">Secs</div>
                </div>
            </div>
        </div>

        <div class="hero-buttons">
            <a href="${pageContext.request.contextPath}/team-login" class="btn btn-primary btn-lg" id="btn-team-login">
                <svg id="btn-login-icon" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4"/>
                    <polyline points="10 17 15 12 10 7"/>
                    <line x1="15" y1="12" x2="3" y2="12"/>
                </svg>
                <span id="btn-login-label">Team Login</span>
            </a>
        </div>
    </section>

    <!-- ===== EVENT INFO SECTION ===== -->
    <section class="hero-info-section">
        <div class="section-ornament">
            <span>Our Flagship Events</span>
        </div>

        <div class="info-card-grid">
            <!-- BizWizX Card -->
            <div class="info-card" id="card-bizwizx">
                <div class="info-card-icon">&#128188;</div>
                <h3>BizWizX</h3>
                <div class="gold-ornament"></div>
                <p>The ultimate business acumen challenge. Four custom-designed rounds testing your knowledge of markets, strategy, finance, and management — all scored live with real-time leaderboards.</p>
            </div>

            <!-- Vortex Card -->
            <div class="info-card" id="card-vortex">
                <div class="info-card-icon">&#127942;</div>
                <h3>Vortex</h3>
                <div class="gold-ornament"></div>
                <p>A whirlwind of applied management. Navigate through <strong>KAIROS, TREORAI, ENMA,</strong> and <strong>GRAND FINALE</strong> — four curated rounds pushing teams to their strategic limits.</p>
            </div>
        </div>
    </section>

    <!-- ===== FOOTER ===== -->
    <footer class="site-footer">
        <span class="footer-brand">PRAGMATRIX 2026</span> &mdash; Applied Management Carnival &bull; All rights reserved.
    </footer>

    <!-- ===== COUNTDOWN BANNER & LOGIN LOCK SCRIPT ===== -->
    <script>
        (function() {
            // Set to true before event day to re-enable the Aug 24 9:00 AM IST lock
            const LOCK_ENABLED = false;

            // Target: August 24, 2026 9:00 AM IST (UTC+5:30) = 03:30:00 UTC on Aug 24, 2026
            const TARGET_TIME = Date.UTC(2026, 7, 24, 3, 30, 0);
            const banner = document.getElementById('live-countdown-banner');
            const loginBtn = document.getElementById('btn-team-login');
            const loginLabel = document.getElementById('btn-login-label');
            const elDays = document.getElementById('countdown-days');
            const elHours = document.getElementById('countdown-hours');
            const elMinutes = document.getElementById('countdown-minutes');
            const elSeconds = document.getElementById('countdown-seconds');

            function updateCountdown() {
                const now = Date.now();
                const diff = TARGET_TIME - now;

                if (diff <= 0) {
                    if (banner) {
                        banner.style.display = 'none';
                    }
                    if (loginBtn) {
                        loginBtn.classList.remove('btn-locked-state');
                    }
                    if (loginLabel) {
                        loginLabel.textContent = 'Team Login';
                    }
                    return false;
                }

                if (banner && banner.style.display === 'none') {
                    banner.style.display = 'block';
                }

                if (LOCK_ENABLED) {
                    if (loginBtn && !loginBtn.classList.contains('btn-locked-state')) {
                        loginBtn.classList.add('btn-locked-state');
                    }
                    if (loginLabel) {
                        loginLabel.textContent = 'Team Login (Opens Aug 24 @ 9:00 AM)';
                    }
                } else {
                    if (loginBtn) {
                        loginBtn.classList.remove('btn-locked-state');
                    }
                    if (loginLabel) {
                        loginLabel.textContent = 'Team Login';
                    }
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
                const timer = setInterval(function() {
                    if (!updateCountdown()) {
                        clearInterval(timer);
                    }
                }, 1000);
            }
        })();
    </script>
</body>
</html>
