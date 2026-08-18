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
    <style>
        .crit-input {
            width: 90px;
            text-align: center;
            font-weight: 700;
            font-size: 1.05rem;
            color: var(--purple-700);
            padding: 0.4rem 0.5rem;
        }
        .subtotal-badge {
            font-weight: 700;
            color: var(--gold-700);
            background: rgba(212, 175, 55, 0.15);
            padding: 0.25rem 0.65rem;
            border-radius: 4px;
            display: inline-flex;
            align-items: center;
            gap: 0.35rem;
        }
        /* Filter tabs */
        .filter-tabs {
            display: flex;
            gap: 0.5rem;
            flex-wrap: wrap;
            margin-bottom: 1rem;
        }
        .filter-tab {
            padding: 0.4rem 0.85rem;
            border-radius: 20px;
            border: 1px solid var(--glass-border);
            background: rgba(255, 255, 255, 0.6);
            color: var(--gray-700);
            font-size: 0.85rem;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.2s ease;
        }
        .filter-tab:hover {
            border-color: var(--purple-400);
            color: var(--purple-700);
        }
        .filter-tab.active {
            background: var(--purple-700);
            color: #ffffff;
            border-color: var(--purple-700);
            box-shadow: 0 2px 8px rgba(58, 16, 101, 0.25);
        }
        /* Accordion cards */
        .team-accordion-card {
            border: 1px solid var(--glass-border);
            border-radius: var(--radius-md);
            background: rgba(255, 255, 255, 0.8);
            margin-bottom: 0.85rem;
            overflow: hidden;
            transition: all 0.25s ease;
            box-shadow: var(--shadow-sm);
        }
        .team-accordion-card.open {
            border-color: var(--gold-500);
            box-shadow: 0 4px 20px rgba(212, 175, 55, 0.2);
            background: rgba(255, 255, 255, 0.95);
        }
        .team-accordion-card.highlight {
            animation: highlightPulse 2s ease;
        }
        @keyframes highlightPulse {
            0% { box-shadow: 0 0 0 4px rgba(212, 175, 55, 0.6); }
            50% { box-shadow: 0 0 0 8px rgba(212, 175, 55, 0.2); }
            100% { box-shadow: var(--shadow-sm); }
        }
        .team-accordion-header {
            padding: 0.85rem 1.25rem;
            display: flex;
            justify-content: space-between;
            align-items: center;
            gap: 1rem;
            cursor: pointer;
            user-select: none;
            transition: background 0.15s ease;
        }
        .team-accordion-header:hover {
            background: rgba(124, 58, 237, 0.04);
        }
        .team-accordion-card.open .team-accordion-header {
            background: rgba(212, 175, 55, 0.08);
            border-bottom: 1px solid rgba(212, 175, 55, 0.25);
        }
        .team-accordion-body {
            display: none;
            padding: 1.25rem 1.5rem;
            animation: fadeIn 0.2s ease;
        }
        .team-accordion-card.open .team-accordion-body {
            display: block;
        }
        @keyframes fadeIn {
            from { opacity: 0; transform: translateY(-4px); }
            to { opacity: 1; transform: translateY(0); }
        }
        .badge-scored {
            background: rgba(34, 197, 94, 0.12);
            color: #15803d;
            border: 1px solid rgba(34, 197, 94, 0.3);
            font-size: 0.8rem;
            padding: 0.25rem 0.6rem;
            border-radius: 4px;
            font-weight: 600;
        }
        .badge-unscored {
            background: rgba(156, 163, 175, 0.15);
            color: #4b5563;
            border: 1px solid rgba(156, 163, 175, 0.3);
            font-size: 0.8rem;
            padding: 0.25rem 0.6rem;
            border-radius: 4px;
            font-weight: 600;
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
                <a href="${pageContext.request.contextPath}/admin/dashboard?quiz=${round.quizCode}">Dashboard</a>
                <c:if test="${round.quizCode == 'VORTEX'}">
                    <a href="${pageContext.request.contextPath}/admin/manage-criteria?roundId=${not empty vortexRound ? vortexRound.roundId : round.roundNumber}">Manage Criteria</a>
                </c:if>
                <a href="${pageContext.request.contextPath}/admin/leaderboard?quiz=${round.quizCode}">Leaderboard</a>
                <a href="${pageContext.request.contextPath}/admin/admins">Admins</a>
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

        <!-- Page Header -->
        <div class="d-flex justify-between align-center flex-wrap gap-md mb-2">
            <div>
                <h1 class="page-title" style="margin-bottom: 0.25rem;">
                    Score Entry: <c:out value="${round.roundName}"/>
                </h1>
                <p class="page-subtitle" style="margin-bottom: 0;">
                    Event: <strong><c:out value="${round.quizCode}"/></strong> &bull; Round ${round.roundNumber}
                    <c:if test="${round.finished}">
                        <span class="badge" style="background: rgba(239, 68, 68, 0.15); color: #ef4444; border: 1px solid rgba(239, 68, 68, 0.3); margin-left: 0.5rem;">
                            Round Locked (Finished)
                        </span>
                    </c:if>
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
            <div class="alert alert-success" id="alert-success" style="margin-bottom: 1.25rem;">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>
                <c:out value="${param.success}"/>
            </div>
        </c:if>
        <c:if test="${not empty param.error}">
            <div class="alert alert-error" style="margin-bottom: 1.25rem;">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
                <c:out value="${param.error}"/>
            </div>
        </c:if>

        <form action="${pageContext.request.contextPath}/admin/score-entry" method="POST" id="score-form">
            <input type="hidden" name="roundId" value="${round.roundId}">
            <input type="hidden" name="targetTeam" id="targetTeamInput" value="">

            <!-- ========================================== -->
            <!-- VORTEX Collapsible / Scalable Score Entry -->
            <!-- ========================================== -->
            <c:choose>
                <c:when test="${round.quizCode == 'VORTEX' && not empty vortexRound && not empty vortexRound.components}">

                    <!-- Filter & Search Toolbar -->
                    <div class="glass-panel mb-3" style="padding: 1.25rem;">
                        <div class="d-flex justify-between align-center flex-wrap gap-md">
                            <!-- Search -->
                            <div class="search-bar" style="flex: 1; min-width: 250px; max-width: 480px;">
                                <svg class="search-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                    <circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/>
                                </svg>
                                <input type="text" id="score-search" placeholder="Search team by ID, College, or Lead..." class="form-control" autocomplete="off">
                            </div>

                            <!-- Filter Tabs -->
                            <div class="filter-tabs" style="margin-bottom: 0;">
                                <button type="button" class="filter-tab active" data-filter="all" id="filter-all">
                                    All Teams (<span id="count-all">${teams.size()}</span>)
                                </button>
                                <button type="button" class="filter-tab" data-filter="scored" id="filter-scored">
                                    ✓ Scored (<span id="count-scored">0</span>)
                                </button>
                                <button type="button" class="filter-tab" data-filter="unscored" id="filter-unscored">
                                    — Unscored (<span id="count-unscored">0</span>)
                                </button>
                            </div>
                        </div>
                    </div>

                    <!-- Teams Collapsible Container -->
                    <div id="teams-accordion-list">
                        <c:forEach var="team" items="${teams}" varStatus="status">
                            <c:set var="tScores" value="${teamCriterionScores[team.uniqueId]}"/>
                            <c:set var="exScore" value="${existingScores[team.uniqueId]}"/>
                            <c:set var="hasScore" value="${not empty exScore && exScore.points > 0 || not empty tScores && tScores.size() > 0}"/>

                            <div class="team-accordion-card"
                                 id="team-card-${team.uniqueId}"
                                 data-team-id="${team.uniqueId}"
                                 data-college="<c:out value='${team.collegeName}'/>"
                                 data-lead="<c:out value='${team.teamLeadName}'/>"
                                 data-scored="${hasScore ? 'true' : 'false'}">

                                <!-- Accordion Header (Compact Row) -->
                                <div class="team-accordion-header" onclick="handleHeaderClick('${team.uniqueId}', event)">
                                    <div class="d-flex align-center gap-md" style="flex: 1; min-width: 0;">
                                        <span style="font-size: 0.85rem; color: var(--gray-400); font-weight: 700; width: 24px;">
                                            ${status.index + 1}.
                                        </span>
                                        <div style="min-width: 0;">
                                            <div class="d-flex align-center gap-sm flex-wrap">
                                                <strong style="color: var(--purple-700); font-family: var(--font-display); font-size: 1.05rem; letter-spacing: 0.5px;">
                                                    <c:out value="${team.uniqueId}"/>
                                                </strong>
                                                <span style="font-weight: 600; color: var(--gray-800);">
                                                    &mdash; <c:out value="${team.collegeName}"/>
                                                </span>
                                            </div>
                                            <div style="font-size: 0.8rem; color: var(--gray-500); margin-top: 0.15rem;">
                                                Lead: <c:out value="${team.teamLeadName}"/>
                                                <c:if test="${not empty team.member2Name}"> &bull; <c:out value="${team.member2Name}"/></c:if>
                                                <c:if test="${not empty team.member3Name}"> &bull; <c:out value="${team.member3Name}"/></c:if>
                                            </div>
                                        </div>
                                    </div>

                                    <!-- Status & Total -->
                                    <div class="d-flex align-center gap-md">
                                        <div class="team-status-box" id="status-box-${team.uniqueId}">
                                            <c:choose>
                                                <c:when test="${hasScore}">
                                                    <span class="badge-scored" id="header-badge-${team.uniqueId}">
                                                        &#10003; Scored (<span id="header-total-${team.uniqueId}"><fmt:formatNumber value="${exScore.points}" minFractionDigits="2" maxFractionDigits="2"/></span> / ${vortexRound.totalMaxMarks})
                                                    </span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge-unscored" id="header-badge-${team.uniqueId}">
                                                        &mdash; Not Scored
                                                    </span>
                                                </c:otherwise>
                                            </c:choose>
                                        </div>

                                        <button type="button"
                                                class="btn btn-sm btn-outline btn-toggle-team"
                                                id="btn-toggle-${team.uniqueId}"
                                                onclick="toggleTeamAccordion('${team.uniqueId}'); event.stopPropagation();">
                                            <c:choose>
                                                <c:when test="${round.finished}">View Scores</c:when>
                                                <c:when test="${hasScore}">Edit Scores</c:when>
                                                <c:otherwise>Enter Scores</c:otherwise>
                                            </c:choose>
                                        </button>
                                    </div>
                                </div>

                                <!-- Accordion Body (Detailed Scoring UI for this Team) -->
                                <div class="team-accordion-body" id="body-${team.uniqueId}">
                                    <div class="d-flex justify-between align-center flex-wrap gap-md mb-2" style="background: rgba(124,58,237,0.04); padding: 0.75rem 1rem; border-radius: var(--radius-sm);">
                                        <div>
                                            <span style="font-size: 0.85rem; color: var(--gray-600);">Scoring for:</span>
                                            <strong style="color: var(--purple-700); font-family: var(--font-display); font-size: 1rem;"><c:out value="${team.uniqueId}"/></strong>
                                            <span style="color: var(--gray-600); font-size: 0.85rem;">(<c:out value="${team.collegeName}"/>)</span>
                                        </div>
                                        <div class="d-flex align-center gap-sm">
                                            <span style="font-size: 0.9rem; color: var(--gray-600); font-weight: 600;">Subtotal:</span>
                                            <span class="subtotal-badge" id="grand_total_${team.uniqueId}" style="font-size: 1.15rem;">
                                                <fmt:formatNumber value="${not empty exScore ? exScore.points : 0}" minFractionDigits="2" maxFractionDigits="2"/> / ${vortexRound.totalMaxMarks}
                                            </span>
                                        </div>
                                    </div>

                                    <!-- Components & Criteria Table -->
                                    <c:forEach var="comp" items="${vortexRound.components}">
                                        <div style="margin-top: 1rem; margin-bottom: 1.25rem;">
                                            <div class="d-flex justify-between align-center mb-1">
                                                <h4 style="margin: 0; color: var(--gold-700); font-size: 0.95rem;">
                                                    <c:out value="${comp.componentLabel}"/>
                                                </h4>
                                                <span style="font-size: 0.8rem; color: var(--gray-500);">
                                                    Component Max: <strong>${comp.maxMarks} Marks</strong>
                                                </span>
                                            </div>

                                            <div class="table-wrapper">
                                                <table class="themed-table">
                                                    <thead>
                                                        <tr>
                                                            <th>Criterion</th>
                                                            <th>Judges Look For</th>
                                                            <th style="width: 80px; text-align: center;">Max</th>
                                                            <th style="width: 130px; text-align: center;">Score</th>
                                                        </tr>
                                                    </thead>
                                                    <tbody>
                                                        <c:forEach var="crit" items="${comp.criteria}">
                                                            <c:set var="scoreVal" value="${tScores[crit.criterionId]}"/>
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
                                                                <td>
                                                                    <input type="number"
                                                                           name="score_${team.uniqueId}_${crit.criterionId}"
                                                                           class="form-control crit-input score-input score-field-${team.uniqueId}"
                                                                           data-team="${team.uniqueId}"
                                                                           data-max="${crit.maxMarks}"
                                                                           step="0.5" min="0" max="${crit.maxMarks}"
                                                                           value="<c:out value='${scoreVal}'/>"
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

                                    <!-- Bottom Action Bar inside Accordion -->
                                    <div class="d-flex justify-between align-center flex-wrap gap-md pt-2" style="border-top: 1px solid var(--glass-border); margin-top: 1rem;">
                                        <button type="button" class="btn btn-outline btn-sm" onclick="toggleTeamAccordion('${team.uniqueId}')">
                                            Close
                                        </button>
                                        <c:if test="${!round.finished}">
                                            <button type="button" class="btn btn-primary btn-sm" onclick="saveTeamScores('${team.uniqueId}')" id="btn-save-${team.uniqueId}">
                                                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="vertical-align: text-bottom; margin-right: 4px;">
                                                    <path d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z"/>
                                                    <polyline points="17 21 17 13 7 13 7 21"/>
                                                    <polyline points="7 3 7 8 15 8"/>
                                                </svg>
                                                Save Scores for <c:out value="${team.uniqueId}"/>
                                            </button>
                                        </c:if>
                                    </div>

                                </div>
                            </div>
                        </c:forEach>
                    </div>

                    <!-- Empty Search Message -->
                    <div id="no-matching-teams" class="glass-panel text-center" style="display: none; padding: 2.5rem; margin-top: 1rem;">
                        <p style="color: var(--gray-600); margin: 0; font-size: 1rem;">No matching teams found for the current search/filter.</p>
                    </div>

                    <c:if test="${empty teams}">
                        <div class="glass-panel text-center" style="padding: 3rem;">
                            <h3 style="color: var(--gray-700); font-family: var(--font-display);">
                                <c:choose>
                                    <c:when test="${round.roundNumber == 4}">No Teams Advanced to GRAND FINALE</c:when>
                                    <c:otherwise>No Registered Teams Found</c:otherwise>
                                </c:choose>
                            </h3>
                            <p style="color: var(--gray-600); margin-bottom: 1.5rem;">
                                <c:choose>
                                    <c:when test="${round.roundNumber == 4}">
                                        Advance the top 3 teams based on cumulative standings after Round 3 (ENMA) from the VORTEX Admin Dashboard.
                                    </c:when>
                                    <c:otherwise>
                                        There are no teams registered for VORTEX yet.
                                    </c:otherwise>
                                </c:choose>
                            </p>
                            <a href="${pageContext.request.contextPath}/admin/dashboard?quiz=VORTEX" class="btn btn-primary">
                                &larr; Return to Dashboard
                            </a>
                        </div>
                    </c:if>

                </c:when>

                <c:when test="${round.quizCode == 'VORTEX'}">
                    <div class="glass-panel text-center" style="padding: 3rem;">
                        <h3 style="color: var(--gray-600);">No Judging Criteria Configured</h3>
                        <p style="color: var(--gray-500); margin-bottom: 1.5rem;">
                            Judging criteria and components have not been set up for this VORTEX round yet.
                        </p>
                        <c:if test="${not empty vortexRound}">
                            <a href="${pageContext.request.contextPath}/admin/manage-criteria?roundId=${vortexRound.roundId}" class="btn btn-primary">
                                Set Up Criteria for <c:out value="${round.roundName}"/>
                            </a>
                        </c:if>
                    </div>
                </c:when>

                <c:otherwise>
                    <!-- ========================================== -->
                    <!-- BIZWIZX Simple Score Entry View           -->
                    <!-- ========================================== -->
                    <div class="glass-panel">
                        <div class="d-flex justify-between align-center flex-wrap gap-md mb-2">
                            <h3 class="section-title mb-0">
                                <span class="title-accent">&#9830;</span> Simple Total Marks Entry
                            </h3>
                            <!-- Search for BIZWIZX -->
                            <div class="search-bar" style="max-width: 350px;">
                                <svg class="search-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                    <circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/>
                                </svg>
                                <input type="text" id="biz-search" placeholder="Filter by ID, College, or Lead..." class="form-control" autocomplete="off">
                            </div>
                        </div>

                        <div class="table-wrapper">
                            <table class="themed-table" id="biz-table">
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
                                        <tr class="biz-row" data-search="<c:out value='${team.uniqueId} ${team.collegeName} ${team.teamLeadName}'/>">
                                            <td>${status.index + 1}</td>
                                            <td><strong style="color: var(--purple-700); font-family: var(--font-display);"><c:out value="${team.uniqueId}"/></strong></td>
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
                                                No eligible teams registered for <c:out value="${round.quizCode}"/> yet.
                                            </td>
                                        </tr>
                                    </c:if>
                                </tbody>
                            </table>
                        </div>
                    </div>

                    <c:if test="${!round.finished && not empty teams}">
                        <div class="d-flex justify-end gap-sm" style="margin-top: 1.5rem;">
                            <a href="${pageContext.request.contextPath}/admin/dashboard?quiz=${round.quizCode}" class="btn btn-outline btn-lg">Cancel</a>
                            <button type="submit" class="btn btn-primary btn-lg" id="btn-save-scores">
                                Save Scores
                            </button>
                        </div>
                    </c:if>

                </c:otherwise>
            </c:choose>
        </form>

    </div>

    <!-- Footer -->
    <footer class="site-footer">
        <span class="footer-brand">PRAGMATRIX 2026</span> &mdash; Admin Score Entry
    </footer>

    <!-- Scripts -->
    <script>
    var totalMaxMarks = ${not empty vortexRound ? vortexRound.totalMaxMarks : 0};
    var isRoundFinished = ${round.finished ? 'true' : 'false'};

    // Handle header click to toggle accordion
    function handleHeaderClick(teamId, event) {
        // Prevent toggle if clicking inside an interactive element
        if (event.target.tagName === 'INPUT' || event.target.tagName === 'BUTTON' || event.target.tagName === 'A') {
            return;
        }
        toggleTeamAccordion(teamId);
    }

    // Accordion expand/collapse (One team open at a time)
    function toggleTeamAccordion(teamId) {
        var card = document.getElementById('team-card-' + teamId);
        if (!card) return;

        var isOpen = card.classList.contains('open');

        // Close all other cards first (single active accordion item)
        document.querySelectorAll('.team-accordion-card.open').forEach(function(c) {
            c.classList.remove('open');
            var btn = c.querySelector('.btn-toggle-team');
            if (btn) {
                var scored = c.getAttribute('data-scored') === 'true';
                btn.textContent = isRoundFinished ? 'View Scores' : (scored ? 'Edit Scores' : 'Enter Scores');
            }
        });

        if (!isOpen) {
            card.classList.add('open');
            var btn = document.getElementById('btn-toggle-' + teamId);
            if (btn) btn.textContent = 'Close';

            // Focus the first score input in this card
            setTimeout(function() {
                var firstInput = card.querySelector('.crit-input:not([disabled])');
                if (firstInput) firstInput.focus();
            }, 100);
        }
    }

    // Save single team scores
    function saveTeamScores(teamId) {
        document.getElementById('targetTeamInput').value = teamId;
        document.getElementById('score-form').submit();
    }

    // Live calculation for VORTEX criteria scores
    function recalculateTeamTotals() {
        var scoredCount = 0;
        var unscoredCount = 0;

        var cards = document.querySelectorAll('.team-accordion-card');
        cards.forEach(function(card) {
            var teamId = card.getAttribute('data-team-id');
            var total = 0;
            var hasAnyScore = false;

            document.querySelectorAll('.score-field-' + teamId).forEach(function(input) {
                var val = parseFloat(input.value);
                var max = parseFloat(input.getAttribute('data-max')) || 0;

                // Visual validation
                if (!isNaN(val) && val > max) {
                    input.style.borderColor = '#ef4444';
                    input.style.backgroundColor = 'rgba(239, 68, 68, 0.08)';
                } else {
                    input.style.borderColor = '';
                    input.style.backgroundColor = '';
                }

                if (!isNaN(val) && val >= 0) {
                    total += val;
                    hasAnyScore = true;
                }
            });

            // Update subtotal in body
            var bodyTotalLabel = document.getElementById('grand_total_' + teamId);
            if (bodyTotalLabel) {
                bodyTotalLabel.textContent = total.toFixed(2) + ' / ' + totalMaxMarks;
            }

            // Update badge in compact header
            var headerBadge = document.getElementById('header-badge-' + teamId);
            if (headerBadge) {
                if (hasAnyScore) {
                    headerBadge.className = 'badge-scored';
                    headerBadge.innerHTML = '&#10003; Scored (' + total.toFixed(2) + ' / ' + totalMaxMarks + ')';
                    card.setAttribute('data-scored', 'true');
                    scoredCount++;
                } else {
                    headerBadge.className = 'badge-unscored';
                    headerBadge.innerHTML = '&mdash; Not Scored';
                    card.setAttribute('data-scored', 'false');
                    unscoredCount++;
                }
            }
        });

        // Update counts in filter tabs
        var countAll = document.getElementById('count-all');
        if (countAll) countAll.textContent = cards.length;
        var countScored = document.getElementById('count-scored');
        if (countScored) countScored.textContent = scoredCount;
        var countUnscored = document.getElementById('count-unscored');
        if (countUnscored) countUnscored.textContent = unscoredCount;
    }

    // Attach live calculate event listeners
    document.querySelectorAll('input[data-team]').forEach(function(input) {
        input.addEventListener('input', recalculateTeamTotals);
    });

    // Client-side search and filtering
    var currentFilter = 'all';
    var currentSearch = '';

    function applyFilters() {
        var cards = document.querySelectorAll('.team-accordion-card');
        var visibleCount = 0;

        cards.forEach(function(card) {
            var teamId = (card.getAttribute('data-team-id') || '').toLowerCase();
            var college = (card.getAttribute('data-college') || '').toLowerCase();
            var lead = (card.getAttribute('data-lead') || '').toLowerCase();
            var isScored = card.getAttribute('data-scored') === 'true';

            var matchesFilter = true;
            if (currentFilter === 'scored' && !isScored) matchesFilter = false;
            if (currentFilter === 'unscored' && isScored) matchesFilter = false;

            var matchesSearch = true;
            if (currentSearch) {
                matchesSearch = teamId.includes(currentSearch) || college.includes(currentSearch) || lead.includes(currentSearch);
            }

            if (matchesFilter && matchesSearch) {
                card.style.display = 'block';
                visibleCount++;
            } else {
                card.style.display = 'none';
            }
        });

        var noMatchEl = document.getElementById('no-matching-teams');
        if (noMatchEl) {
            noMatchEl.style.display = (visibleCount === 0 && cards.length > 0) ? 'block' : 'none';
        }
    }

    // Search bar event
    var scoreSearchInput = document.getElementById('score-search');
    if (scoreSearchInput) {
        scoreSearchInput.addEventListener('input', function() {
            currentSearch = this.value.trim().toLowerCase();
            applyFilters();
        });
    }

    // Filter tabs click
    document.querySelectorAll('.filter-tab').forEach(function(tab) {
        tab.addEventListener('click', function() {
            document.querySelectorAll('.filter-tab').forEach(function(t) { t.classList.remove('active'); });
            this.classList.add('active');
            currentFilter = this.getAttribute('data-filter');
            applyFilters();
        });
    });

    // BIZWIZX live filter
    var bizSearchInput = document.getElementById('biz-search');
    if (bizSearchInput) {
        bizSearchInput.addEventListener('input', function() {
            var q = this.value.trim().toLowerCase();
            document.querySelectorAll('.biz-row').forEach(function(row) {
                var text = (row.getAttribute('data-search') || '').toLowerCase();
                row.style.display = text.includes(q) ? '' : 'none';
            });
        });
    }

    // Initial setup & auto-expand saved team
    (function() {
        recalculateTeamTotals();

        // Check if a savedTeam is requested in URL
        var urlParams = new URLSearchParams(window.location.search);
        var savedTeam = urlParams.get('savedTeam');
        if (savedTeam) {
            var card = document.getElementById('team-card-' + savedTeam);
            if (card) {
                toggleTeamAccordion(savedTeam);
                card.classList.add('highlight');
                card.scrollIntoView({ behavior: 'smooth', block: 'center' });
            }
        }
    })();

    // Auto-dismiss alerts
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
