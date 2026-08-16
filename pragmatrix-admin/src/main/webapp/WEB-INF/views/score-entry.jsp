<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Score Entry — <c:out value="${round.roundName}"/> — PRAGMATRIX 2026</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/theme.css">
    <style>
        .crit-input {
            width: 80px;
            text-align: center;
            font-weight: 700;
            font-size: 1rem;
            color: var(--purple-700);
        }
        .subtotal-badge {
            font-weight: 700;
            color: var(--gold-700);
            background: rgba(212, 175, 55, 0.15);
            padding: 0.25rem 0.6rem;
            border-radius: 4px;
        }
    </style>
</head>
<body>

    <!-- ===== HEADER ===== -->
    <header class="site-header">
        <nav class="header-nav">
            <a href="${pageContext.request.contextPath}/" class="header-brand">
                <img src="${pageContext.request.contextPath}/images/set-logo.jpg" alt="Seshadripuram Educational Trust" class="brand-logo" style="border-radius: 50%;">
                <span class="brand-text">Pragmatrix 2026</span>
            </a>
            <div class="header-links">
                <a href="${pageContext.request.contextPath}/admin/dashboard">Dashboard</a>
                <c:if test="${round.quizCode == 'VORTEX'}">
                    <a href="${pageContext.request.contextPath}/admin/manage-criteria?roundId=${vortexRound.roundId}">Manage Criteria</a>
                </c:if>
                <a href="${pageContext.request.contextPath}/admin/leaderboard?quiz=${round.quizCode}">Leaderboard</a>
            </div>
            <div class="admin-info">
                <span class="admin-name">
                    Logged in as <span class="name-highlight"><c:out value="${sessionScope.adminName}"/></span>
                </span>
                <a href="${pageContext.request.contextPath}/admin/logout" class="btn btn-sm btn-outline" style="color: var(--gold-300); border-color: var(--gold-600);">Logout</a>
            </div>
        </nav>
    </header>

    <!-- ===== MAIN CONTENT ===== -->
    <div class="page-container">

        <div class="d-flex justify-between align-center flex-wrap gap-md mb-2">
            <div>
                <h1 class="page-title">
                    Score Entry: <c:out value="${round.roundName}"/>
                </h1>
                <p class="page-subtitle">
                    Event: <strong><c:out value="${round.quizCode}"/></strong> | Round ${round.roundNumber}
                </p>
            </div>
            <div class="d-flex gap-sm">
                <c:if test="${round.quizCode == 'VORTEX' && not empty vortexRound}">
                    <a href="${pageContext.request.contextPath}/admin/manage-criteria?roundId=${vortexRound.roundId}" class="btn btn-outline btn-sm">
                        Edit Judging Criteria
                    </a>
                </c:if>
                <a href="${pageContext.request.contextPath}/admin/dashboard?quiz=${round.quizCode}" class="btn btn-outline btn-sm">
                    &larr; Back to Dashboard
                </a>
            </div>
        </div>

        <!-- Alerts -->
        <c:if test="${not empty param.success}">
            <div class="alert alert-success">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>
                <c:out value="${param.success}"/>
            </div>
        </c:if>
        <c:if test="${not empty param.error}">
            <div class="alert alert-error">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
                <c:out value="${param.error}"/>
            </div>
        </c:if>

        <form action="${pageContext.request.contextPath}/admin/score-entry" method="POST" id="score-form">
            <input type="hidden" name="roundId" value="${round.roundId}">

            <!-- VORTEX Detailed Score Entry View -->
            <c:choose>
                <c:when test="${round.quizCode == 'VORTEX' && not empty vortexRound && not empty vortexRound.components}">

                    <c:forEach var="team" items="${teams}">
                        <c:set var="tScores" value="${teamCriterionScores[team.uniqueId]}"/>

                        <div class="glass-panel mb-3">
                            <div class="d-flex justify-between align-center flex-wrap gap-md mb-2" style="padding-bottom: 0.75rem; border-bottom: 1px solid rgba(212,175,55,0.2);">
                                <div>
                                    <h3 class="section-title mb-0">
                                        <span class="title-accent">&#9830;</span>
                                        Team: <span style="color: var(--purple-700); font-family: var(--font-display);"><c:out value="${team.uniqueId}"/></span>
                                        &mdash; <c:out value="${team.collegeName}"/>
                                    </h3>
                                    <p style="margin: 0.25rem 0 0 0; color: var(--gray-600); font-size: 0.85rem;">
                                        Lead: <c:out value="${team.teamLeadName}"/>
                                    </p>
                                </div>
                                <div>
                                    <span style="font-size: 0.9rem; color: var(--gray-600);">Total Score:</span>
                                    <span class="subtotal-badge" id="grand_total_${team.uniqueId}" style="font-size: 1.2rem;">0.00 / ${vortexRound.totalMaxMarks}</span>
                                </div>
                            </div>

                            <c:forEach var="comp" items="${vortexRound.components}">
                                <div style="margin-top: 1rem; margin-bottom: 1.25rem;">
                                    <div class="d-flex justify-between align-center mb-1">
                                        <h4 style="margin: 0; color: var(--gold-600); font-size: 1rem;">
                                            <c:out value="${comp.componentLabel}"/>
                                        </h4>
                                        <span style="font-size: 0.85rem; color: var(--gray-500);">
                                            Component Max: <strong>${comp.maxMarks} Marks</strong>
                                        </span>
                                    </div>

                                    <div class="table-wrapper">
                                        <table class="themed-table">
                                            <thead>
                                                <tr>
                                                    <th style="width: 40%;">Criterion</th>
                                                    <th style="width: 40%;">Judges Look For</th>
                                                    <th style="width: 10%; text-align: center;">Max</th>
                                                    <th style="width: 10%; text-align: center;">Score</th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                <c:forEach var="crit" items="${comp.criteria}">
                                                    <c:set var="cVal" value="${tScores[crit.criterionId]}"/>
                                                    <tr>
                                                        <td><strong><c:out value="${crit.criterionName}"/></strong></td>
                                                        <td style="color: var(--gray-600); font-style: italic; font-size: 0.85rem;">
                                                            <c:choose>
                                                                <c:when test="${not empty crit.judgesLookFor}">
                                                                    &ldquo;<c:out value="${crit.judgesLookFor}"/>&rdquo;
                                                                </c:when>
                                                                <c:otherwise>&mdash;</c:otherwise>
                                                            </c:choose>
                                                        </td>
                                                        <td style="text-align: center;"><strong style="color: var(--gold-700);">${crit.maxMarks}</strong></td>
                                                        <td style="text-align: center;">
                                                            <input type="number"
                                                                   name="score_${team.uniqueId}_${crit.criterionId}"
                                                                   class="form-control crit-input score-field-${team.uniqueId}"
                                                                   data-team="${team.uniqueId}"
                                                                   data-max="${crit.maxMarks}"
                                                                   step="0.5" min="0" max="${crit.maxMarks}"
                                                                   value="<c:out value='${cVal}'/>"
                                                                   placeholder="0"
                                                                   <c:if test="${round.finished}">disabled</c:if>>
                                                        </td>
                                                    </tr>
                                                </c:forEach>
                                            </tbody>
                                        </table>
                                    </div>
                                </div>
                            </c:forEach>
                        </div>
                    </c:forEach>

                </c:when>
                <c:otherwise>

                    <!-- BIZWIZX Simple Score Entry View -->
                    <div class="glass-panel">
                        <h3 class="section-title">
                            <span class="title-accent">&#9830;</span> Simple Total Marks Entry
                        </h3>
                        <div class="table-wrapper">
                            <table class="themed-table">
                                <thead>
                                    <tr>
                                        <th>#</th>
                                        <th>Unique ID</th>
                                        <th>College Name</th>
                                        <th>Team Lead</th>
                                        <th>Points Awarded</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="team" items="${teams}" varStatus="status">
                                        <c:set var="existingScore" value="${existingScores[team.uniqueId]}"/>
                                        <tr>
                                            <td>${status.index + 1}</td>
                                            <td><strong style="color: var(--purple-700);"><c:out value="${team.uniqueId}"/></strong></td>
                                            <td><c:out value="${team.collegeName}"/></td>
                                            <td><c:out value="${team.teamLeadName}"/></td>
                                            <td>
                                                <input type="number"
                                                       name="score_${team.uniqueId}"
                                                       class="form-control"
                                                       style="width: 120px; font-weight: bold; text-align: center;"
                                                       step="0.5" min="0" max="999"
                                                       value="<c:out value='${existingScore.points}'/>"
                                                       placeholder="0.00"
                                                       <c:if test="${round.finished}">disabled</c:if>>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                    <c:if test="${empty teams}">
                                        <tr>
                                            <td colspan="5" style="text-align: center; color: var(--gray-500); padding: 2rem;">
                                                No teams registered for <c:out value="${round.quizCode}"/> yet.
                                            </td>
                                        </tr>
                                    </c:if>
                                </tbody>
                            </table>
                        </div>
                    </div>

                </c:otherwise>
            </c:choose>

            <c:if test="${!round.finished && not empty teams}">
                <div class="d-flex justify-end gap-sm style-sticky" style="margin-top: 1.5rem;">
                    <a href="${pageContext.request.contextPath}/admin/dashboard?quiz=${round.quizCode}" class="btn btn-outline btn-lg">Cancel</a>
                    <button type="submit" class="btn btn-primary btn-lg" id="btn-save-scores">
                        Save Scores
                    </button>
                </div>
            </c:if>
        </form>

    </div>

    <!-- Footer -->
    <footer class="site-footer">
        <span class="footer-brand">PRAGMATRIX 2026</span> &mdash; Admin Score Entry
    </footer>

    <script>
    // Live calculation for VORTEX criteria scores
    function recalculateTeamTotals() {
        var teams = new Set();
        document.querySelectorAll('input[data-team]').forEach(function(input) {
            teams.add(input.getAttribute('data-team'));
        });

        teams.forEach(function(teamId) {
            var total = 0;
            document.querySelectorAll('.score-field-' + teamId).forEach(function(input) {
                var val = parseFloat(input.value);
                if (!isNaN(val)) {
                    total += val;
                }
            });
            var label = document.getElementById('grand_total_' + teamId);
            if (label) {
                var maxText = label.textContent.split('/')[1] || '';
                label.textContent = total.toFixed(2) + ' /' + maxText;
            }
        });
    }

    document.querySelectorAll('input[data-team]').forEach(function(input) {
        input.addEventListener('input', recalculateTeamTotals);
    });

    // Run initial calculation
    recalculateTeamTotals();
    </script>
</body>
</html>
