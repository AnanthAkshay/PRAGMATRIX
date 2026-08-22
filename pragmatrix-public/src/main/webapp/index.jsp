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
            <div class="countdown-banner-inner">
                <div class="countdown-badge">
                    <span class="pulse-icon"></span> Live Event Notice
                </div>
                <div class="countdown-title">
                    PRAGMATRIX 2026 goes live on <strong>August 24, 2026 at 8:00 AM IST</strong>
                </div>
                <div class="countdown-timer">
                    <div class="timer-segment">
                        <span id="cd-days" class="timer-val">00</span>
                        <span class="timer-lbl">Days</span>
                    </div>
                    <span class="timer-sep">:</span>
                    <div class="timer-segment">
                        <span id="cd-hours" class="timer-val">00</span>
                        <span class="timer-lbl">Hours</span>
                    </div>
                    <span class="timer-sep">:</span>
                    <div class="timer-segment">
                        <span id="cd-minutes" class="timer-val">00</span>
                        <span class="timer-lbl">Mins</span>
                    </div>
                    <span class="timer-sep">:</span>
                    <div class="timer-segment">
                        <span id="cd-seconds" class="timer-val">00</span>
                        <span class="timer-lbl">Secs</span>
                    </div>
                </div>
            </div>
        </div>

        <script>
            (function() {
                // August 24, 2026 8:00 AM IST = 02:30:00 UTC (Fixed UTC timestamp)
                var TARGET_TIMESTAMP = new Date("2026-08-24T08:00:00+05:30").getTime();

                function updateCountdown() {
                    var banner = document.getElementById('live-countdown-banner');
                    if (!banner) return;

                    var now = Date.now();
                    var diff = TARGET_TIMESTAMP - now;

                    if (diff <= 0) {
                        banner.style.display = 'none';
                        return;
                    }

                    var days = Math.floor(diff / (1000 * 60 * 60 * 24));
                    var hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
                    var minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));
                    var seconds = Math.floor((diff % (1000 * 60)) / 1000);

                    var dEl = document.getElementById('cd-days');
                    var hEl = document.getElementById('cd-hours');
                    var mEl = document.getElementById('cd-minutes');
                    var sEl = document.getElementById('cd-seconds');

                    if (dEl) dEl.textContent = String(days).padStart(2, '0');
                    if (hEl) hEl.textContent = String(hours).padStart(2, '0');
                    if (mEl) mEl.textContent = String(minutes).padStart(2, '0');
                    if (sEl) sEl.textContent = String(seconds).padStart(2, '0');

                    banner.style.display = 'block';
                }

                updateCountdown();
                var timerInterval = setInterval(function() {
                    var now = Date.now();
                    if (TARGET_TIMESTAMP - now <= 0) {
                        clearInterval(timerInterval);
                        var banner = document.getElementById('live-countdown-banner');
                        if (banner) banner.style.display = 'none';
                    } else {
                        updateCountdown();
                    }
                }, 1000);
            })();
        </script>

        <div class="hero-buttons">
            <a href="${pageContext.request.contextPath}/team-login" class="btn btn-primary btn-lg" id="btn-team-login">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4"/>
                    <polyline points="10 17 15 12 10 7"/>
                    <line x1="15" y1="12" x2="3" y2="12"/>
                </svg>
                Team Login
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

</body>
</html>
