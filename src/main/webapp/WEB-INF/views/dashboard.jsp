<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Admin Dashboard — PRAGMATRIX 2026</title>
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
                <a href="${pageContext.request.contextPath}/admin/dashboard" class="active">Dashboard</a>
                <a href="${pageContext.request.contextPath}/admin/leaderboard?quiz=${selectedQuiz}">Leaderboard</a>
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

        <!-- Page Title -->
        <h1 class="page-title">Admin Dashboard</h1>
        <p class="page-subtitle">Manage quizzes, rounds, teams, and scores</p>

        <!-- Alerts -->
        <c:if test="${not empty param.success}">
            <div class="alert alert-success" id="alert-success">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>
                <c:out value="${param.success}"/>
            </div>
        </c:if>
        <c:if test="${not empty param.error}">
            <div class="alert alert-error" id="alert-error">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
                <c:out value="${param.error}"/>
            </div>
        </c:if>

        <!-- ===== QUIZ TABS ===== -->
        <div class="quiz-tabs">
            <a href="${pageContext.request.contextPath}/admin/dashboard?quiz=BIZWIZX"
               class="quiz-tab <c:if test='${selectedQuiz == "BIZWIZX"}'>active</c:if>"
               id="tab-bizwizx">BizWizX</a>
            <a href="${pageContext.request.contextPath}/admin/dashboard?quiz=VORTEX"
               class="quiz-tab <c:if test='${selectedQuiz == "VORTEX"}'>active</c:if>"
               id="tab-vortex">Vortex</a>
        </div>

        <!-- ===== STATS ROW ===== -->
        <div class="card-grid" style="grid-template-columns: repeat(auto-fill, minmax(160px, 1fr)); margin-bottom: 1.5rem;">
            <div class="stat-card">
                <div class="stat-value" id="stat-teams"><c:out value="${teamCount}"/></div>
                <div class="stat-label">Teams Registered</div>
            </div>
            <c:set var="finishedCount" value="0"/>
            <c:forEach var="r" items="${rounds}">
                <c:if test="${r.finished}"><c:set var="finishedCount" value="${finishedCount + 1}"/></c:if>
            </c:forEach>
            <div class="stat-card">
                <div class="stat-value"><c:out value="${finishedCount}"/> / <c:out value="${fn:length(rounds)}"/></div>
                <div class="stat-label">Rounds Finished</div>
            </div>
            <div class="stat-card">
                <div class="stat-value" style="font-size: 1.2rem; padding-top: 0.4rem;">
                    <c:out value="${selectedQuiz}"/>
                </div>
                <div class="stat-label">Active Quiz</div>
            </div>
        </div>

        <!-- ===== ROUND MANAGEMENT ===== -->
        <div class="glass-panel mb-3">
            <h3 class="section-title">
                <span class="title-accent">&#9830;</span> Round Management
            </h3>
            <div class="card-grid">
                <c:forEach var="round" items="${rounds}">
                    <div class="round-card <c:if test='${round.finished}'>finished</c:if>" id="round-card-${round.roundId}">
                        <div class="round-header">
                            <span class="round-number">Round <c:out value="${round.roundNumber}"/></span>
                            <c:choose>
                                <c:when test="${round.finished}">
                                    <span class="status-badge finished">
                                        <span class="status-dot active"></span> Finished
                                    </span>
                                </c:when>
                                <c:otherwise>
                                    <span class="status-badge pending">
                                        <span class="status-dot pending"></span> Active
                                    </span>
                                </c:otherwise>
                            </c:choose>
                        </div>

                        <div class="round-title"><c:out value="${round.roundName}"/></div>
                        <div class="round-criteria">
                            <c:choose>
                                <c:when test="${not empty round.judgingCriteria}">
                                    <c:out value="${round.judgingCriteria}"/>
                                </c:when>
                                <c:otherwise>
                                    <em style="color: var(--gray-400);">No criteria set</em>
                                </c:otherwise>
                            </c:choose>
                        </div>

                        <!-- Edit Form (hidden until toggled) -->
                        <c:if test="${!round.finished}">
                            <div class="round-edit-form" id="edit-form-${round.roundId}" style="display:none;">
                                <form action="${pageContext.request.contextPath}/admin/round-manage" method="POST">
                                    <input type="hidden" name="roundId" value="${round.roundId}">
                                    <input type="hidden" name="quizCode" value="${selectedQuiz}">

                                    <c:if test="${selectedQuiz == 'BIZWIZX'}">
                                        <div class="form-group">
                                            <label class="form-label">Round Name</label>
                                            <input type="text" name="roundName" class="form-control" value="<c:out value='${round.roundName}'/>" maxlength="100">
                                        </div>
                                    </c:if>

                                    <div class="form-group">
                                        <label class="form-label">Judging Criteria</label>
                                        <input type="text" name="judgingCriteria" class="form-control"
                                               value="<c:out value='${round.judgingCriteria}'/>"
                                               placeholder="One-line criteria..."
                                               maxlength="255">
                                    </div>

                                    <div class="d-flex gap-sm">
                                        <button type="submit" class="btn btn-primary btn-sm">Save</button>
                                        <button type="button" class="btn btn-outline btn-sm" onclick="toggleEdit(${round.roundId})">Cancel</button>
                                    </div>
                                </form>
                            </div>
                        </c:if>

                        <!-- Actions -->
                        <div class="round-actions">
                            <a href="${pageContext.request.contextPath}/admin/score-entry?roundId=${round.roundId}"
                               class="btn btn-primary btn-sm" id="btn-scores-${round.roundId}">
                                <c:choose>
                                    <c:when test="${round.finished}">View Scores</c:when>
                                    <c:otherwise>Enter Scores</c:otherwise>
                                </c:choose>
                            </a>

                            <c:if test="${!round.finished}">
                                <button type="button" class="btn btn-outline btn-sm" onclick="toggleEdit(${round.roundId})" id="btn-edit-${round.roundId}">
                                    Edit
                                </button>
                            </c:if>

                            <c:choose>
                                <c:when test="${!round.finished}">
                                    <form action="${pageContext.request.contextPath}/admin/finish-round" method="POST" style="display:inline;" onsubmit="return confirmFinish('${round.roundName}')">
                                        <input type="hidden" name="roundId" value="${round.roundId}">
                                        <input type="hidden" name="quizCode" value="${selectedQuiz}">
                                        <input type="hidden" name="action" value="finish">
                                        <button type="submit" class="btn btn-success btn-sm" id="btn-finish-${round.roundId}">Finish Round</button>
                                    </form>
                                </c:when>
                                <c:otherwise>
                                    <form action="${pageContext.request.contextPath}/admin/finish-round" method="POST" style="display:inline;" onsubmit="return confirm('Reopen this round for score corrections?')">
                                        <input type="hidden" name="roundId" value="${round.roundId}">
                                        <input type="hidden" name="quizCode" value="${selectedQuiz}">
                                        <input type="hidden" name="action" value="reopen">
                                        <button type="submit" class="btn btn-danger btn-sm" id="btn-reopen-${round.roundId}">Reopen</button>
                                    </form>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>
                </c:forEach>
            </div>
        </div>

        <!-- ===== TEAM LIST ===== -->
        <div class="glass-panel">
            <div class="d-flex justify-between align-center flex-wrap gap-md mb-2">
                <h3 class="section-title mb-0">
                    <span class="title-accent">&#9830;</span> Registered Teams
                </h3>

                <!-- Search -->
                <div class="search-bar">
                    <svg class="search-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/>
                    </svg>
                    <input type="text" id="team-search" placeholder="Search by ID or College..."
                           value="<c:out value='${searchQuery}'/>"
                           data-quiz="${selectedQuiz}">
                </div>
            </div>

            <div class="table-wrapper" id="team-table-wrapper">
                <table class="themed-table" id="team-table">
                    <thead>
                        <tr>
                            <th>#</th>
                            <th>Unique ID</th>
                            <th>College</th>
                            <th>Student 1</th>
                            <th>Student 2</th>
                            <th>Student 3</th>
                            <th>Total Points</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody id="team-tbody">
                        <c:forEach var="team" items="${teams}" varStatus="status">
                            <tr>
                                <td>${status.index + 1}</td>
                                <td><strong style="color: var(--purple-700);"><c:out value="${team.uniqueId}"/></strong></td>
                                <td><c:out value="${team.collegeName}"/></td>
                                <td><c:out value="${team.student1Name}"/></td>
                                <td><c:out value="${team.student2Name}" default="—"/></td>
                                <td><c:out value="${team.student3Name}" default="—"/></td>
                                <td>
                                    <strong style="color: var(--gold-700);">
                                        <c:out value="${team.totalPoints}"/>
                                    </strong>
                                </td>
                                <td>
                                    <a href="${pageContext.request.contextPath}/admin/scorecard?id=${team.uniqueId}"
                                       class="btn btn-outline btn-sm" title="View Scorecard">
                                        Scorecard
                                    </a>
                                </td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty teams}">
                            <tr>
                                <td colspan="8" style="text-align: center; padding: 2rem; color: var(--gray-500);">
                                    No teams registered for <c:out value="${selectedQuiz}"/> yet.
                                </td>
                            </tr>
                        </c:if>
                    </tbody>
                </table>
            </div>
        </div>

    </div><!-- /page-container -->

    <!-- Footer -->
    <footer class="site-footer">
        <span class="footer-brand">PRAGMATRIX 2026</span> &mdash; Admin Dashboard
    </footer>

    <script>
    /* Toggle round edit form */
    function toggleEdit(roundId) {
        var form = document.getElementById('edit-form-' + roundId);
        if (form) {
            form.style.display = form.style.display === 'none' ? 'block' : 'none';
        }
    }

    /* Confirm finish round */
    function confirmFinish(roundName) {
        return confirm('Are you sure you want to finish "' + roundName + '"? This will lock score editing for this round.');
    }

    /* Live search with debounce */
    (function() {
        var searchInput = document.getElementById('team-search');
        var timer;

        searchInput.addEventListener('input', function() {
            clearTimeout(timer);
            var query = this.value.trim();
            var quiz = this.getAttribute('data-quiz');

            timer = setTimeout(function() {
                if (query.length === 0) {
                    /* Reload without search */
                    window.location.href = '${pageContext.request.contextPath}/admin/dashboard?quiz=' + quiz;
                    return;
                }
                window.location.href = '${pageContext.request.contextPath}/admin/dashboard?quiz=' + quiz + '&search=' + encodeURIComponent(query);
            }, 600);
        });

        /* Handle Enter key */
        searchInput.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                clearTimeout(timer);
                var query = this.value.trim();
                var quiz = this.getAttribute('data-quiz');
                window.location.href = '${pageContext.request.contextPath}/admin/dashboard?quiz=' + quiz +
                    (query ? '&search=' + encodeURIComponent(query) : '');
            }
        });
    })();

    /* Auto-dismiss alerts after 5 seconds */
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
