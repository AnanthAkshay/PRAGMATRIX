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
        <!-- Logo centered in archway stage -->
        <!-- archway-bg.jpeg is actually the PRAGMATRIX logo (filenames are swapped) -->
        <img src="${pageContext.request.contextPath}/images/archway-bg.jpeg"
             alt="PRAGMATRIX 2026 — Applied Management Carnival"
             class="hero-logo"
             id="hero-logo">

        <p class="hero-tagline">Inter-College Applied Management Carnival</p>

        <div class="hero-buttons">
            <a href="${pageContext.request.contextPath}/register" class="btn btn-primary btn-lg" id="btn-register">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/>
                    <circle cx="9" cy="7" r="4"/>
                    <line x1="19" y1="8" x2="19" y2="14"/>
                    <line x1="22" y1="11" x2="16" y2="11"/>
                </svg>
                Register a Team
            </a>
            <a href="${pageContext.request.contextPath}/login" class="btn btn-secondary btn-lg" id="btn-admin-login">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
                    <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
                </svg>
                Admin Login
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
                <p>A whirlwind of applied management. Navigate through <strong>KAIROS, THEORAI, ENMA,</strong> and <strong>SLANCIO</strong> — four curated rounds pushing teams to their strategic limits.</p>
            </div>
        </div>
    </section>

    <!-- ===== FOOTER ===== -->
    <footer class="site-footer">
        <span class="footer-brand">PRAGMATRIX 2026</span> &mdash; Applied Management Carnival &bull; All rights reserved.
    </footer>

</body>
</html>
