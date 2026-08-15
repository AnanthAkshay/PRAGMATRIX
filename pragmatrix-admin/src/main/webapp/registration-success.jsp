<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Registration Successful — PRAGMATRIX 2026</title>
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

    <!-- Success Content -->
    <div class="success-container">
        <div class="success-card glass-panel">
            <!-- Success Icon -->
            <div class="success-icon">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round">
                    <polyline points="20 6 9 17 4 12"/>
                </svg>
            </div>

            <h2>Registration Successful!</h2>
            <p class="text-muted" style="margin-top: 0.5rem;">Your team has been registered for PRAGMATRIX 2026.</p>

            <div class="gold-ornament"></div>

            <p style="margin-bottom: 0.5rem; font-weight: 600; color: var(--purple-700);">Your Unique Team ID</p>

            <!-- Large ID Display -->
            <div class="unique-id-display" id="team-unique-id">
                <c:out value="${uniqueId}"/>
            </div>

            <div class="alert alert-warning" style="text-align: left; margin-top: 1rem;">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>
                <strong>Save this ID!</strong>&nbsp; Take a screenshot or write it down. You'll need this ID to check your scores and for on-site verification.
            </div>

            <div style="margin-top: 1.5rem; display: flex; gap: 1rem; flex-wrap: wrap; justify-content: center;">
                <a href="${pageContext.request.contextPath}/register" class="btn btn-outline">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
                    Register Another Team
                </a>
                <a href="${pageContext.request.contextPath}/" class="btn btn-secondary">
                    &larr; Back to Home
                </a>
            </div>
        </div>
    </div>

</body>
</html>
