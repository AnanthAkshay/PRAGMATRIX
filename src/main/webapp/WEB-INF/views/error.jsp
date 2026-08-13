<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Error — PRAGMATRIX 2026</title>
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
                <c:if test="${not empty sessionScope.adminId}">
                    <a href="${pageContext.request.contextPath}/admin/dashboard">Dashboard</a>
                </c:if>
            </div>
        </nav>
    </header>

    <div class="centered-form-wrapper">
        <div class="centered-form-card glass-panel" style="text-align: center;">

            <!-- Error Icon -->
            <div style="width: 80px; height: 80px; border-radius: 50%; background: linear-gradient(135deg, var(--error), #D32F2F); display: flex; align-items: center; justify-content: center; margin: 0 auto 1.5rem;">
                <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                    <circle cx="12" cy="12" r="10"/>
                    <line x1="15" y1="9" x2="9" y2="15"/>
                    <line x1="9" y1="9" x2="15" y2="15"/>
                </svg>
            </div>

            <h2 style="color: var(--error); margin-bottom: 0.75rem;">
                <c:choose>
                    <c:when test="${pageContext.errorData.statusCode == 404}">Page Not Found</c:when>
                    <c:when test="${pageContext.errorData.statusCode == 500}">Server Error</c:when>
                    <c:otherwise>Something Went Wrong</c:otherwise>
                </c:choose>
            </h2>

            <div class="gold-ornament"></div>

            <c:choose>
                <c:when test="${not empty error}">
                    <p style="color: var(--gray-600); margin-bottom: 1.5rem;">
                        <c:out value="${error}"/>
                    </p>
                </c:when>
                <c:when test="${pageContext.errorData.statusCode == 404}">
                    <p style="color: var(--gray-600); margin-bottom: 1.5rem;">
                        The page you're looking for doesn't exist or has been moved.
                    </p>
                </c:when>
                <c:otherwise>
                    <p style="color: var(--gray-600); margin-bottom: 1.5rem;">
                        An unexpected error occurred. Please try again or contact support.
                    </p>
                </c:otherwise>
            </c:choose>

            <c:if test="${pageContext.errorData.statusCode != null}">
                <div style="font-family: var(--font-display); font-size: 3rem; font-weight: 900; color: var(--gray-200); margin-bottom: 1.5rem;">
                    ${pageContext.errorData.statusCode}
                </div>
            </c:if>

            <div class="d-flex justify-center gap-md flex-wrap">
                <a href="${pageContext.request.contextPath}/" class="btn btn-primary">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
                    Go Home
                </a>
                <c:if test="${not empty sessionScope.adminId}">
                    <a href="${pageContext.request.contextPath}/admin/dashboard" class="btn btn-secondary">
                        Dashboard
                    </a>
                </c:if>
            </div>
        </div>
    </div>

</body>
</html>
