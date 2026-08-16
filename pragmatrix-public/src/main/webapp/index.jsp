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
                <p>A whirlwind of applied management. Navigate through <strong>TREORAI, KAIROS, ENMA,</strong> and <strong>GRAND FINALE</strong> — four curated rounds pushing teams to their strategic limits.</p>
            </div>
        </div>
    </section>

    <!-- ===== FOOTER ===== -->
    <footer class="site-footer">
        <span class="footer-brand">PRAGMATRIX 2026</span> &mdash; Applied Management Carnival &bull; All rights reserved.
    </footer>

</body>
</html>
