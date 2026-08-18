<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Scorecard — <c:out value="${team.uniqueId}"/> — PRAGMATRIX 2026</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/theme.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/print.css">
</head>
<body>

    <!-- ===== HEADER ===== -->
    <header class="site-header no-print">
        <nav class="header-nav">
            <a href="${pageContext.request.contextPath}/" class="header-brand">
                <img src="${pageContext.request.contextPath}/images/archway-bg.jpeg" alt="PRAGMATRIX" class="brand-logo">
                <span class="brand-text">Pragmatrix 2026</span>
            </a>
            <div class="header-links">
                <a href="${pageContext.request.contextPath}/admin/dashboard?quiz=${team.quizCode}">Dashboard</a>
                <a href="${pageContext.request.contextPath}/admin/leaderboard?quiz=${team.quizCode}">Leaderboard</a>
                <a href="${pageContext.request.contextPath}/admin/admins">Admins</a>
            </div>
            <div class="admin-info">
                <span class="admin-name">
                    <span class="name-highlight"><c:out value="${sessionScope.adminName}"/></span>
                </span>
                <a href="${pageContext.request.contextPath}/admin/logout" class="btn btn-sm btn-outline" style="color: var(--gold-300); border-color: var(--gold-600);">Logout</a>
            </div>
        </nav>
    </header>

    <div class="page-container">

        <!-- Back link + Print button -->
        <div class="d-flex justify-between align-center flex-wrap gap-md mb-2 no-print">
            <a href="${pageContext.request.contextPath}/admin/dashboard?quiz=${team.quizCode}" class="btn btn-outline btn-sm">
                &larr; Back to Dashboard
            </a>
            <div class="d-flex gap-sm">
                <a href="${pageContext.request.contextPath}/admin/edit-team?uniqueId=${team.uniqueId}" class="btn btn-outline" id="btn-edit-team">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path>
                        <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path>
                    </svg>
                    Edit Team
                </a>
                <button onclick="window.print()" class="btn btn-primary" id="btn-print-scorecard">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <polyline points="6 9 6 2 18 2 18 9"/><path d="M6 18H4a2 2 0 0 1-2-2v-5a2 2 0 0 1 2-2h16a2 2 0 0 1 2 2v5a2 2 0 0 1-2 2h-2"/><rect x="6" y="14" width="12" height="8"/>
                    </svg>
                    Print Scorecard
                </button>
            </div>
        </div>

        <!-- ===== SCORECARD ===== -->
        <div class="scorecard glass-panel">

            <!-- Scorecard Header -->
            <div class="scorecard-header">
                <img src="${pageContext.request.contextPath}/images/archway-bg.jpeg"
                     alt="PRAGMATRIX 2026"
                     class="scorecard-logo">
                <h1>PRAGMATRIX 2026</h1>
                <div class="scorecard-event">
                    <c:choose>
                        <c:when test="${team.quizCode == 'BIZWIZX'}">BizWizX — Business Quiz</c:when>
                        <c:when test="${team.quizCode == 'VORTEX'}">Vortex — Applied Management</c:when>
                        <c:otherwise><c:out value="${team.quizCode}"/></c:otherwise>
                    </c:choose>
                </div>
            </div>

            <div class="gold-divider"></div>

            <!-- Team Info -->
            <div class="scorecard-team-info">
                <div class="info-item">
                    <div class="info-label">Team ID</div>
                    <div class="info-value" style="color: var(--purple-700); font-weight: 700; font-family: var(--font-display); letter-spacing: 1px;">
                        <c:out value="${team.uniqueId}"/>
                    </div>
                </div>
                <div class="info-item">
                    <div class="info-label">College</div>
                    <div class="info-value"><c:out value="${team.collegeName}"/></div>
                </div>
                <div class="info-item">
                    <div class="info-label">Team Lead</div>
                    <div class="info-value"><c:out value="${team.teamLeadName}"/></div>
                </div>
                <div class="info-item">
                    <div class="info-label">Lead Email</div>
                    <div class="info-value"><c:out value="${team.leadEmail}"/></div>
                </div>
                <c:if test="${not empty team.member2Name}">
                    <div class="info-item">
                        <div class="info-label">Member 2</div>
                        <div class="info-value"><c:out value="${team.member2Name}"/></div>
                    </div>
                </c:if>
                <c:if test="${not empty team.member3Name}">
                    <div class="info-item">
                        <div class="info-label">Member 3</div>
                        <div class="info-value"><c:out value="${team.member3Name}"/></div>
                    </div>
                </c:if>
            </div>

            <div class="gold-divider"></div>

            <!-- Round-wise Breakdown -->
            <h3 class="section-title">
                <span class="title-accent">&#9830;</span> Round-wise Breakdown
            </h3>

            <div class="table-wrapper">
                <table class="themed-table">
                    <thead>
                        <tr>
                            <th>Round</th>
                            <th>Round Name</th>
                            <th>Judging Criteria</th>
                            <th>Status</th>
                            <th style="text-align: center;">Points</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="round" items="${rounds}">
                            <c:set var="score" value="${scores[round.roundId]}"/>
                            <tr>
                                <td>
                                    <span style="font-family: var(--font-display); font-weight: 700; color: var(--purple-600);">
                                        R<c:out value="${round.roundNumber}"/>
                                    </span>
                                </td>
                                <td><strong><c:out value="${round.roundName}"/></strong></td>
                                <td>
                                    <c:choose>
                                        <c:when test="${not empty round.judgingCriteria}">
                                            <span style="font-style: italic; color: var(--gray-600);">
                                                <c:out value="${round.judgingCriteria}"/>
                                            </span>
                                        </c:when>
                                        <c:otherwise>
                                            <span style="color: var(--gray-400);">—</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${round.finished}">
                                            <span class="status-badge finished">
                                                <span class="status-dot active"></span> Finished
                                            </span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="status-badge pending">
                                                <span class="status-dot pending"></span> Pending
                                            </span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td style="text-align: center;">
                                    <c:choose>
                                        <c:when test="${round.finished && score != null}">
                                            <strong style="color: var(--gold-700); font-size: 1.1rem;">
                                                <fmt:formatNumber value="${score.points}" maxFractionDigits="2"/>
                                            </strong>
                                        </c:when>
                                        <c:when test="${score != null}">
                                            <span style="color: var(--gray-400);">
                                                (<fmt:formatNumber value="${score.points}" maxFractionDigits="2"/>)*
                                            </span>
                                        </c:when>
                                        <c:otherwise>
                                            <span style="color: var(--gray-400);">&mdash;</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>

            <!-- Total -->
            <div class="scorecard-total">
                <div class="total-label">Total Points (Finished Rounds)</div>
                <div class="total-value">
                    <fmt:formatNumber value="${totalPoints}" maxFractionDigits="2"/>
                </div>
            </div>

            <!-- Print Signature Line -->
            <div class="print-signature">
                Verified by: ___________________________ &nbsp;&nbsp;&nbsp; Date: _______________
            </div>
        </div>

    </div><!-- /page-container -->

    <footer class="site-footer no-print">
        <span class="footer-brand">PRAGMATRIX 2026</span> &mdash; Scorecard
    </footer>

</body>
</html>
