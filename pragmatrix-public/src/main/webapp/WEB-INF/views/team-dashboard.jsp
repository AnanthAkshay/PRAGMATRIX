<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Team Dashboard — PRAGMATRIX 2026</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/theme.css">
    <style>
        .team-info-grid {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
            gap: 1rem;
            margin-bottom: 1.5rem;
        }
        .team-info-item {
            padding: 0.75rem;
        }
        .team-info-item .info-label {
            font-size: 0.7rem;
            font-weight: 700;
            color: var(--purple-600);
            text-transform: uppercase;
            letter-spacing: 1px;
            margin-bottom: 0.2rem;
        }
        .team-info-item .info-value {
            font-size: 1rem;
            font-weight: 500;
            color: var(--gray-800);
        }
        .live-indicator {
            display: inline-flex;
            align-items: center;
            gap: 0.4rem;
            font-size: 0.8rem;
            color: var(--gray-500);
            padding: 0.3rem 0.7rem;
            background: var(--glass-bg-light);
            border: 1px solid var(--glass-border);
            border-radius: var(--radius-full);
        }
        .live-dot {
            width: 8px;
            height: 8px;
            border-radius: 50%;
            background: var(--success);
            animation: pulse-dot 2s ease infinite;
        }
        @keyframes pulse-dot {
            0%, 100% { opacity: 1; }
            50% { opacity: 0.3; }
        }
        .score-pending {
            color: var(--gray-400);
            font-style: italic;
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
                <a href="${pageContext.request.contextPath}/">Home</a>
            </div>
            <div class="admin-info">
                <span class="admin-name">
                    Team <span class="name-highlight"><c:out value="${team.uniqueId}"/></span>
                </span>
                <a href="${pageContext.request.contextPath}/team-logout" class="btn btn-sm btn-outline" style="color: var(--gold-300); border-color: var(--gold-600);">Logout</a>
            </div>
        </nav>
    </header>

    <!-- ===== MAIN CONTENT ===== -->
    <div class="page-container">

        <div class="d-flex justify-between align-center flex-wrap gap-md mb-2">
            <div>
                <h1 class="page-title">Team Dashboard</h1>
                <p class="page-subtitle">Your scores and round progress for <c:out value="${team.quizCode}"/></p>
            </div>
            <div class="live-indicator" id="live-indicator">
                <span class="live-dot"></span>
                <span id="last-updated-text">Live</span>
            </div>
        </div>

        <!-- ===== TEAM INFO ===== -->
        <div class="glass-panel mb-3">
            <h3 class="section-title">
                <span class="title-accent">&#9830;</span> Team Information
            </h3>
            <div class="team-info-grid" id="team-info">
                <div class="team-info-item">
                    <div class="info-label">Participant ID</div>
                    <div class="info-value" style="font-family: var(--font-display); color: var(--purple-700); font-weight: 700; letter-spacing: 2px;">
                        <c:out value="${team.uniqueId}"/>
                    </div>
                </div>
                <div class="team-info-item">
                    <div class="info-label">Quiz Event</div>
                    <div class="info-value" style="font-family: var(--font-display); color: var(--gold-700);">
                        <c:out value="${team.quizCode}"/>
                    </div>
                </div>
                <div class="team-info-item">
                    <div class="info-label">College</div>
                    <div class="info-value"><c:out value="${team.collegeName}"/></div>
                </div>
                <div class="team-info-item">
                    <div class="info-label">Team Lead</div>
                    <div class="info-value"><c:out value="${team.teamLeadName}"/></div>
                </div>
                <c:if test="${team.eliminated}">
                    <div class="team-info-item" id="team-status-item">
                        <div class="info-label">Status</div>
                        <div class="info-value">
                            <span class="badge" style="background: rgba(239, 68, 68, 0.15); color: #ef4444; border: 1px solid rgba(239, 68, 68, 0.3); font-size: 0.85rem; padding: 0.25rem 0.6rem; border-radius: 4px; font-weight: 600;">
                                Eliminated
                            </span>
                        </div>
                    </div>
                </c:if>
            </div>
        </div>

        <!-- ===== STATS ROW ===== -->
        <div class="card-grid" style="grid-template-columns: minmax(180px, 300px); margin-bottom: 1.5rem;">
            <div class="stat-card">
                <div class="stat-value" id="stat-total-points"><c:out value="${totalPoints}"/></div>
                <div class="stat-label">Total Points</div>
            </div>
        </div>

        <!-- ===== ROUND-WISE SCORES ===== -->
        <div class="glass-panel">
            <h3 class="section-title">
                <span class="title-accent">&#9830;</span> Round-wise Scores
            </h3>

            <div class="table-wrapper">
                <table class="themed-table" id="scores-table">
                    <thead>
                        <tr>
                            <th>#</th>
                            <th>Round Name</th>
                            <th>Judging Criteria</th>
                            <th>Status</th>
                            <th>Points</th>
                            <th>Scoresheet</th>
                        </tr>
                    </thead>
                    <tbody id="scores-tbody">
                        <c:forEach var="round" items="${rounds}">
                            <c:set var="roundScore" value="${scoreMap[round.roundId]}"/>
                            <c:set var="hasScore" value="${not empty roundScore && not empty roundScore.points}"/>
                            <c:set var="critScores" value="${teamDetailedScores[round.roundNumber]}"/>
                            <c:set var="vRound" value="${vortexRoundsMap[round.roundNumber]}"/>
                            <tr id="round-row-${round.roundNumber}">
                                <td><c:out value="${round.roundNumber}"/></td>
                                <td style="font-weight: 600; color: var(--purple-800);">
                                    <c:out value="${round.roundName}"/>
                                </td>
                                <td style="font-size: 0.85rem; color: var(--gray-600); font-style: italic;">
                                    <c:choose>
                                        <c:when test="${not empty round.judgingCriteria}">
                                            <c:out value="${round.judgingCriteria}"/>
                                        </c:when>
                                        <c:otherwise>&mdash;</c:otherwise>
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
                                                <span class="status-dot pending"></span> In Progress
                                            </span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${round.finished && hasScore}">
                                            <strong style="color: var(--gold-700); font-size: 1.1rem;">
                                                <c:out value="${roundScore.points}"/>
                                            </strong>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="score-pending">Pending</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${hasScore || (not empty critScores && fn:length(critScores) > 0)}">
                                            <button type="button" class="btn btn-sm btn-primary"
                                                    onclick="openScoresheetModal(${round.roundNumber})"
                                                    style="padding: 0.25rem 0.6rem; font-size: 0.8rem; display: inline-flex; align-items: center; gap: 0.3rem;">
                                                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/></svg>
                                                View Scoresheet
                                            </button>
                                        </c:when>
                                        <c:when test="${team.quizCode == 'VORTEX' && not empty vRound && not empty vRound.components}">
                                            <button type="button" class="btn btn-sm btn-outline"
                                                    onclick="openCriteriaModal(${round.roundNumber})"
                                                    style="color: var(--purple-700); border-color: var(--purple-600); padding: 0.25rem 0.6rem; font-size: 0.8rem;">
                                                View Judging Criteria
                                            </button>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="score-pending" style="font-size: 0.85rem;">Not yet scored</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty rounds}">
                            <tr>
                                <td colspan="6" style="text-align: center; padding: 2rem; color: var(--gray-500);">
                                    No rounds configured yet.
                                </td>
                            </tr>
                        </c:if>
                    </tbody>
                </table>
            </div>

            <!-- Total Points Display -->
            <div class="scorecard-total" style="margin-top: 1.25rem;">
                <div class="total-label">Total Points</div>
                <div class="total-value" id="total-points-display"><c:out value="${totalPoints}"/></div>
            </div>
        </div>

    </div><!-- /page-container -->

    <!-- ===== READ-ONLY SCORESHEET MODAL ===== -->
    <div id="scoresheet-modal" style="display: none; position: fixed; inset: 0; background: rgba(0,0,0,0.75); backdrop-filter: blur(6px); z-index: 9999; justify-content: center; align-items: center; padding: 1rem;">
        <div class="glass-panel" style="max-width: 850px; width: 100%; max-height: 90vh; overflow-y: auto; position: relative; border: 1px solid var(--gold-600); box-shadow: 0 12px 40px rgba(0,0,0,0.5);">
            <div class="d-flex justify-between align-center mb-2" style="border-bottom: 1px solid rgba(212,175,55,0.2); padding-bottom: 0.75rem;">
                <h3 style="font-family: var(--font-display); color: var(--gold-700); margin: 0;" id="scoresheet-modal-title">
                    Scoresheet
                </h3>
                <button type="button" onclick="closeScoresheetModal()" style="background: none; border: none; font-size: 1.5rem; color: var(--gray-400); cursor: pointer;">&times;</button>
            </div>

            <!-- Team Header Summary Bar (Mirroring Admin Score-Entry Header) -->
            <div class="glass-panel mb-3" style="background: rgba(255,255,255,0.03); border: 1px solid var(--glass-border); padding: 1rem;">
                <div class="d-flex justify-between align-center flex-wrap gap-md">
                    <div>
                        <h3 class="section-title mb-0" style="font-size: 1.1rem;">
                            <span class="title-accent">&#9830;</span>
                            Team: <span style="color: var(--purple-700); font-family: var(--font-display); font-weight: 700;"><c:out value="${team.uniqueId}"/></span>
                            &mdash; <c:out value="${team.collegeName}"/>
                        </h3>
                        <p style="margin: 0.25rem 0 0 0; color: var(--gray-600); font-size: 0.85rem;">
                            Team Lead: <strong><c:out value="${team.teamLeadName}"/></strong>
                        </p>
                    </div>
                    <div class="d-flex align-center gap-sm">
                        <span style="font-size: 0.95rem; color: var(--gray-600); font-weight: 600;">Total Score:</span>
                        <span class="subtotal-badge" id="scoresheet-total-badge" style="font-size: 1.25rem;">0.00</span>
                    </div>
                </div>
            </div>

            <div id="scoresheet-modal-content">
                <c:forEach var="round" items="${rounds}">
                    <c:set var="rScore" value="${scoreMap[round.roundId]}"/>
                    <c:set var="vRound" value="${vortexRoundsMap[round.roundNumber]}"/>
                    <c:set var="tScores" value="${teamDetailedScores[round.roundNumber]}"/>

                    <div class="round-scoresheet-details" id="round-scoresheet-${round.roundNumber}"
                         data-round-name="<c:out value='${round.roundName}'/>"
                         data-total-points="<c:choose><c:when test='${not empty rScore && not empty rScore.points}'><c:out value='${rScore.points}'/></c:when><c:otherwise>0.00</c:otherwise></c:choose>"
                         data-max-marks="${not empty vRound ? vRound.totalMaxMarks : ''}"
                         style="display: none;">

                        <c:choose>
                            <c:when test="${team.quizCode == 'VORTEX' && not empty vRound && not empty vRound.components}">
                                <c:forEach var="comp" items="${vRound.components}">
                                    <div style="margin-bottom: 1.5rem;">
                                        <div class="d-flex justify-between align-center mb-1">
                                            <h4 style="margin: 0; color: var(--gold-600); font-size: 1rem;">
                                                <c:out value="${comp.componentLabel}"/>
                                            </h4>
                                            <span style="font-size: 0.85rem; color: var(--gray-500);">
                                                Component Max: <strong>${comp.maxMarks} Marks</strong>
                                            </span>
                                        </div>

                                        <div class="table-wrapper">
                                            <table class="themed-table" style="font-size: 0.9rem;">
                                                <thead>
                                                    <tr>
                                                        <th style="width: 38%;">Criterion</th>
                                                        <th style="width: 42%;">Judges Look For</th>
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
                                                                <strong style="color: var(--purple-700); font-size: 1.05rem;">
                                                                    <c:choose>
                                                                        <c:when test="${cVal != null}">
                                                                            <c:out value="${cVal}"/>
                                                                        </c:when>
                                                                        <c:otherwise>&mdash;</c:otherwise>
                                                                    </c:choose>
                                                                </strong>
                                                            </td>
                                                        </tr>
                                                    </c:forEach>
                                                    <c:if test="${empty comp.criteria}">
                                                        <tr>
                                                            <td colspan="4" style="text-align: center; color: var(--gray-500);">No criteria defined for this component.</td>
                                                        </tr>
                                                    </c:if>
                                                </tbody>
                                            </table>
                                        </div>
                                    </div>
                                </c:forEach>
                            </c:when>

                            <c:otherwise>
                                <!-- BIZWIZX / Standard Round Scoresheet -->
                                <div style="padding: 1rem 0;">
                                    <div class="table-wrapper">
                                        <table class="themed-table">
                                            <thead>
                                                <tr>
                                                    <th>Round</th>
                                                    <th>Judging Criteria / Notes</th>
                                                    <th style="text-align: center;">Status</th>
                                                    <th style="text-align: center;">Points Awarded</th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                <tr>
                                                    <td><strong><c:out value="${round.roundName}"/></strong> (Round ${round.roundNumber})</td>
                                                    <td style="color: var(--gray-600); font-style: italic;">
                                                        <c:choose>
                                                            <c:when test="${not empty round.judgingCriteria}"><c:out value="${round.judgingCriteria}"/></c:when>
                                                            <c:otherwise>&mdash;</c:otherwise>
                                                        </c:choose>
                                                    </td>
                                                    <td style="text-align: center;">
                                                        <c:choose>
                                                            <c:when test="${round.finished}">
                                                                <span class="status-badge finished"><span class="status-dot active"></span> Finished</span>
                                                            </c:when>
                                                            <c:otherwise>
                                                                <span class="status-badge pending"><span class="status-dot pending"></span> In Progress</span>
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </td>
                                                    <td style="text-align: center;">
                                                        <strong style="color: var(--gold-700); font-size: 1.2rem;">
                                                            <c:choose>
                                                                <c:when test="${not empty rScore && not empty rScore.points}"><c:out value="${rScore.points}"/></c:when>
                                                                <c:otherwise>&mdash;</c:otherwise>
                                                            </c:choose>
                                                        </strong>
                                                    </td>
                                                </tr>
                                            </tbody>
                                        </table>
                                    </div>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </c:forEach>
            </div>

            <div class="d-flex justify-end mt-2" style="padding-top: 0.75rem; border-top: 1px solid rgba(212,175,55,0.2);">
                <button type="button" class="btn btn-primary btn-sm" onclick="closeScoresheetModal()">Close</button>
            </div>
        </div>
    </div>

    <!-- ===== VORTEX JUDGING CRITERIA READ-ONLY MODAL ===== -->
    <c:if test="${team.quizCode == 'VORTEX'}">
        <div id="criteria-modal" style="display: none; position: fixed; inset: 0; background: rgba(0,0,0,0.7); backdrop-filter: blur(5px); z-index: 9999; justify-content: center; align-items: center; padding: 1rem;">
            <div class="glass-panel" style="max-width: 700px; width: 100%; max-height: 85vh; overflow-y: auto; position: relative; border: 1px solid var(--gold-600);">
                <div class="d-flex justify-between align-center mb-2" style="border-bottom: 1px solid rgba(212,175,55,0.2); padding-bottom: 0.75rem;">
                    <h3 style="font-family: var(--font-display); color: var(--gold-700); margin: 0;" id="modal-round-title">
                        Judging Criteria
                    </h3>
                    <button type="button" onclick="closeCriteriaModal()" style="background: none; border: none; font-size: 1.5rem; color: var(--gray-400); cursor: pointer;">&times;</button>
                </div>

                <div id="modal-content">
                    <!-- Loaded dynamically per round -->
                    <c:forEach var="vEntry" items="${vortexRoundsMap}">
                        <c:set var="vRound" value="${vEntry.value}"/>
                        <div class="vround-details" id="vround-details-${vRound.displayOrder}" style="display: none;">
                            <div class="mb-2">
                                <span class="badge" style="background: rgba(147,51,234,0.1); color: var(--purple-700); font-weight: 700;">
                                    Total Round Marks: ${vRound.totalMaxMarks} Marks
                                </span>
                            </div>

                            <c:forEach var="vComp" items="${vRound.components}">
                                <div style="margin-bottom: 1.5rem;">
                                    <h4 style="color: var(--gold-600); margin: 0 0 0.5rem 0; font-size: 1.05rem;">
                                        <c:out value="${vComp.componentLabel}"/>
                                        <span style="font-size: 0.85rem; color: var(--gray-500); font-weight: normal;">
                                            (${vComp.maxMarks} Marks)
                                        </span>
                                    </h4>

                                    <table class="themed-table" style="font-size: 0.9rem;">
                                        <thead>
                                            <tr>
                                                <th>Criterion</th>
                                                <th>Judges Look For</th>
                                                <th style="width: 80px; text-align: center;">Max</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <c:forEach var="vCrit" items="${vComp.criteria}">
                                                <tr>
                                                    <td><strong><c:out value="${vCrit.criterionName}"/></strong></td>
                                                    <td style="color: var(--gray-600); font-style: italic;">
                                                        <c:choose>
                                                            <c:when test="${not empty vCrit.judgesLookFor}">
                                                                &ldquo;<c:out value="${vCrit.judgesLookFor}"/>&rdquo;
                                                            </c:when>
                                                            <c:otherwise>&mdash;</c:otherwise>
                                                        </c:choose>
                                                    </td>
                                                    <td style="text-align: center;"><strong style="color: var(--gold-700);">${vCrit.maxMarks}</strong></td>
                                                </tr>
                                            </c:forEach>
                                            <c:if test="${empty vComp.criteria}">
                                                <tr>
                                                    <td colspan="3" style="text-align: center; color: var(--gray-500);">No criteria defined yet.</td>
                                                </tr>
                                            </c:if>
                                        </tbody>
                                    </table>
                                </div>
                            </c:forEach>

                            <c:if test="${empty vRound.components}">
                                <p style="text-align: center; color: var(--gray-500); padding: 1.5rem;">
                                    Judging criteria for this round will be defined by the organizers soon.
                                </p>
                            </c:if>
                        </div>
                    </c:forEach>
                </div>

                <div class="d-flex justify-end mt-2">
                    <button type="button" class="btn btn-primary btn-sm" onclick="closeCriteriaModal()">Close</button>
                </div>
            </div>
        </div>
    </c:if>

    <!-- Footer -->
    <footer class="site-footer">
        <span class="footer-brand">PRAGMATRIX 2026</span> &mdash; Team Dashboard
    </footer>

    <!-- ===== SCRIPTS ===== -->
    <script>
    function openScoresheetModal(roundNumber) {
        document.querySelectorAll('.round-scoresheet-details').forEach(function(el) { el.style.display = 'none'; });
        var target = document.getElementById('round-scoresheet-' + roundNumber);
        if (target) {
            target.style.display = 'block';
            var roundName = target.getAttribute('data-round-name') || ('Round ' + roundNumber);
            var totalPoints = target.getAttribute('data-total-points') || '0.00';
            var maxMarks = target.getAttribute('data-max-marks');

            document.getElementById('scoresheet-modal-title').textContent = 'Round ' + roundNumber + ': ' + roundName + ' — Scoresheet';

            var badgeText = totalPoints;
            if (maxMarks && maxMarks.trim() !== '') {
                badgeText += ' / ' + maxMarks + ' Marks';
            } else {
                badgeText += ' Points';
            }
            document.getElementById('scoresheet-total-badge').textContent = badgeText;
            document.getElementById('scoresheet-modal').style.display = 'flex';
        }
    }

    function closeScoresheetModal() {
        document.getElementById('scoresheet-modal').style.display = 'none';
    }

    function openCriteriaModal(roundNumber) {
        var modal = document.getElementById('criteria-modal');
        if (!modal) return;
        document.querySelectorAll('.vround-details').forEach(function(el) { el.style.display = 'none'; });
        var target = document.getElementById('vround-details-' + roundNumber);
        if (target) {
            target.style.display = 'block';
            document.getElementById('modal-round-title').textContent = 'Round ' + roundNumber + ' Judging Criteria';
            modal.style.display = 'flex';
        }
    }

    function closeCriteriaModal() {
        var modal = document.getElementById('criteria-modal');
        if (modal) modal.style.display = 'none';
    }

    // ===== LIVE SCORE POLLING =====
    (function() {
        var POLL_INTERVAL = 10000; // 10 seconds
        var contextPath = '${pageContext.request.contextPath}';
        var isVortex = ${team.quizCode == 'VORTEX'};
        var lastUpdatedEl = document.getElementById('last-updated-text');
        var lastFetchTime = Date.now();

        function updateTimestamp() {
            var seconds = Math.floor((Date.now() - lastFetchTime) / 1000);
            if (seconds < 5) {
                lastUpdatedEl.textContent = 'Just now';
            } else if (seconds < 60) {
                lastUpdatedEl.textContent = seconds + 's ago';
            } else {
                lastUpdatedEl.textContent = Math.floor(seconds / 60) + 'm ago';
            }
        }

        function fetchScores() {
            fetch(contextPath + '/team/score-status', {
                credentials: 'same-origin'
            })
            .then(function(response) {
                if (response.status === 401) {
                    window.location.href = contextPath + '/team-login';
                    return null;
                }
                return response.json();
            })
            .then(function(data) {
                if (!data || data.error) return;

                lastFetchTime = Date.now();

                // Update total points
                var totalPoints = data.totalPoints || 0;
                document.getElementById('stat-total-points').textContent = totalPoints;
                document.getElementById('total-points-display').textContent = totalPoints;

                // Update round rows
                if (data.rounds) {
                    var tbody = document.getElementById('scores-tbody');
                    var rows = '';
                    for (var i = 0; i < data.rounds.length; i++) {
                        var r = data.rounds[i];
                        var statusBadge = r.isFinished
                            ? '<span class="status-badge finished"><span class="status-dot active"></span> Finished</span>'
                            : '<span class="status-badge pending"><span class="status-dot pending"></span> In Progress</span>';
                        var pointsDisplay = (r.isFinished && r.points !== null)
                            ? '<strong style="color: var(--gold-700); font-size: 1.1rem;">' + r.points + '</strong>'
                            : '<span class="score-pending">Pending</span>';
                        var criteria = r.judgingCriteria || '\u2014';

                        var actionButton = '';
                        if (r.hasScore || (r.isFinished && r.points !== null)) {
                            actionButton = '<button type="button" class="btn btn-sm btn-primary" onclick="openScoresheetModal(' + r.roundNumber + ')" style="padding: 0.25rem 0.6rem; font-size: 0.8rem; display: inline-flex; align-items: center; gap: 0.3rem;"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/></svg>View Scoresheet</button>';
                        } else if (isVortex) {
                            actionButton = '<button type="button" class="btn btn-sm btn-outline" onclick="openCriteriaModal(' + r.roundNumber + ')" style="color: var(--purple-700); border-color: var(--purple-600); padding: 0.25rem 0.6rem; font-size: 0.8rem;">View Judging Criteria</button>';
                        } else {
                            actionButton = '<span class="score-pending" style="font-size: 0.85rem;">Not yet scored</span>';
                        }

                        rows += '<tr id="round-row-' + r.roundNumber + '">'
                             + '<td>' + r.roundNumber + '</td>'
                             + '<td style="font-weight: 600; color: var(--purple-800);">' + escapeHtml(r.roundName) + '</td>'
                             + '<td style="font-size: 0.85rem; color: var(--gray-600); font-style: italic;">' + escapeHtml(criteria) + '</td>'
                             + '<td>' + statusBadge + '</td>'
                             + '<td>' + pointsDisplay + '</td>'
                             + '<td>' + actionButton + '</td>'
                             + '</tr>';
                    }
                    tbody.innerHTML = rows;
                }
            })
            .catch(function(err) {
                console.error('Score fetch error:', err);
            });
        }

        function escapeHtml(text) {
            var div = document.createElement('div');
            div.appendChild(document.createTextNode(text));
            return div.innerHTML;
        }

        // Poll every 10 seconds
        setInterval(fetchScores, POLL_INTERVAL);

        // Update timestamp display every second
        setInterval(updateTimestamp, 1000);
    })();
    </script>

</body>
</html>
