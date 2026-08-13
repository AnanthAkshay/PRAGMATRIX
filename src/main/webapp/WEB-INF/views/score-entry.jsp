<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Score Entry — <c:out value="${round.roundName}"/> — PRAGMATRIX 2026</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/theme.css">
</head>
<body>

    <!-- ===== HEADER ===== -->
    <header class="site-header">
        <nav class="header-nav">
            <a href="${pageContext.request.contextPath}/" class="header-brand">
                <img src="${pageContext.request.contextPath}/images/archway-bg.jpeg" alt="PRAGMATRIX" class="brand-logo">
                <span class="brand-text">Pragmatrix 2026</span>
            </a>
            <div class="header-links">
                <a href="${pageContext.request.contextPath}/admin/dashboard?quiz=${round.quizCode}">Dashboard</a>
                <a href="${pageContext.request.contextPath}/admin/leaderboard?quiz=${round.quizCode}">Leaderboard</a>
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

        <!-- Breadcrumb / Back -->
        <div class="mb-2">
            <a href="${pageContext.request.contextPath}/admin/dashboard?quiz=${round.quizCode}" style="color: var(--gold-700); font-weight: 500;">
                &larr; Back to Dashboard
            </a>
        </div>

        <!-- Round Info Header -->
        <div class="glass-panel mb-3">
            <div class="d-flex justify-between align-center flex-wrap gap-md">
                <div>
                    <div class="text-sm text-muted" style="text-transform: uppercase; letter-spacing: 1.5px; font-weight: 600;">
                        <c:out value="${round.quizCode}"/> &bull; Round <c:out value="${round.roundNumber}"/>
                    </div>
                    <h2 class="page-title mb-0"><c:out value="${round.roundName}"/></h2>
                    <c:if test="${not empty round.judgingCriteria}">
                        <p class="text-muted mt-1" style="font-style: italic;"><c:out value="${round.judgingCriteria}"/></p>
                    </c:if>
                </div>
                <div>
                    <c:choose>
                        <c:when test="${round.finished}">
                            <span class="status-badge finished" style="font-size: 0.85rem; padding: 0.4rem 1rem;">
                                <span class="status-dot active"></span> Round Finished
                            </span>
                        </c:when>
                        <c:otherwise>
                            <span class="status-badge pending" style="font-size: 0.85rem; padding: 0.4rem 1rem;">
                                <span class="status-dot pending"></span> In Progress
                            </span>
                        </c:otherwise>
                    </c:choose>
                </div>
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
        <c:if test="${not empty error}">
            <div class="alert alert-error">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
                <c:out value="${error}"/>
            </div>
        </c:if>

        <!-- Score Entry Table -->
        <div class="glass-panel">
            <form action="${pageContext.request.contextPath}/admin/score-entry" method="POST" id="score-form">
                <input type="hidden" name="roundId" value="${round.roundId}">

                <div class="d-flex justify-between align-center flex-wrap gap-md mb-2">
                    <h3 class="section-title mb-0">
                        <span class="title-accent">&#9830;</span> Team Scores
                        <span class="text-sm text-muted" style="font-family: var(--font-body); font-weight: 400; margin-left: 0.5rem;">
                            (<c:out value="${teams.size()}"/> teams)
                        </span>
                    </h3>

                    <c:if test="${!round.finished}">
                        <div class="d-flex gap-sm">
                            <button type="submit" class="btn btn-primary btn-sm" id="btn-save-scores">
                                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z"/><polyline points="17 21 17 13 7 13 7 21"/><polyline points="7 3 7 8 15 8"/></svg>
                                Save Scores
                            </button>
                        </div>
                    </c:if>
                </div>

                <div class="table-wrapper">
                    <table class="themed-table" id="scores-table">
                        <thead>
                            <tr>
                                <th>#</th>
                                <th>Unique ID</th>
                                <th>College</th>
                                <th>Student 1</th>
                                <th>Points</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="team" items="${teams}" varStatus="status">
                                <c:set var="existingScore" value="${existingScores[team.uniqueId]}"/>
                                <tr>
                                    <td>${status.index + 1}</td>
                                    <td><strong style="color: var(--purple-700);"><c:out value="${team.uniqueId}"/></strong></td>
                                    <td><c:out value="${team.collegeName}"/></td>
                                    <td><c:out value="${team.student1Name}"/></td>
                                    <td>
                                        <input type="number"
                                               name="score_${team.uniqueId}"
                                               class="score-input"
                                               value="<c:if test='${existingScore != null}'><fmt:formatNumber value='${existingScore.points}' maxFractionDigits='2'/></c:if>"
                                               min="0"
                                               max="999999"
                                               step="0.01"
                                               placeholder="0"
                                               <c:if test="${round.finished}">readonly</c:if>
                                               id="score-${team.uniqueId}">
                                    </td>
                                </tr>
                            </c:forEach>
                            <c:if test="${empty teams}">
                                <tr>
                                    <td colspan="5" style="text-align: center; padding: 2rem; color: var(--gray-500);">
                                        No teams registered for this quiz yet.
                                    </td>
                                </tr>
                            </c:if>
                        </tbody>
                    </table>
                </div>

                <!-- Bottom actions -->
                <c:if test="${!round.finished && not empty teams}">
                    <div class="d-flex justify-between align-center flex-wrap gap-md mt-3">
                        <button type="submit" class="btn btn-primary" id="btn-save-scores-bottom">
                            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z"/><polyline points="17 21 17 13 7 13 7 21"/><polyline points="7 3 7 8 15 8"/></svg>
                            Save All Scores
                        </button>

                        <form action="${pageContext.request.contextPath}/admin/finish-round" method="POST"
                              style="display:inline;" id="finish-round-form"
                              onsubmit="return confirm('Are you sure you want to finish this round? Scores will be locked.');">
                            <input type="hidden" name="roundId" value="${round.roundId}">
                            <input type="hidden" name="quizCode" value="${round.quizCode}">
                            <input type="hidden" name="action" value="finish">
                            <button type="submit" class="btn btn-success" id="btn-finish-round">
                                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>
                                Finish Round
                            </button>
                        </form>
                    </div>
                </c:if>
            </form>
        </div>

    </div><!-- /page-container -->

    <footer class="site-footer">
        <span class="footer-brand">PRAGMATRIX 2026</span> &mdash; Score Entry
    </footer>

    <script>
    /* Validate scores before submit */
    document.getElementById('score-form').addEventListener('submit', function(e) {
        var inputs = document.querySelectorAll('.score-input');
        for (var i = 0; i < inputs.length; i++) {
            var val = inputs[i].value.trim();
            if (val !== '' && (isNaN(parseFloat(val)) || parseFloat(val) < 0)) {
                e.preventDefault();
                inputs[i].style.borderColor = 'var(--error)';
                inputs[i].focus();
                alert('Invalid score for ' + inputs[i].name.replace('score_', '') + '. Points must be non-negative numbers.');
                return;
            }
        }
    });

    /* Auto-dismiss alerts */
    setTimeout(function() {
        var alerts = document.querySelectorAll('.alert');
        alerts.forEach(function(a) {
            a.style.transition = 'opacity 0.5s ease';
            a.style.opacity = '0';
            setTimeout(function() { a.remove(); }, 500);
        });
    }, 5000);
    </script>

</body>
</html>
