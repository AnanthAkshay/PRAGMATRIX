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
                <img src="${pageContext.request.contextPath}/images/set-logo.jpg" alt="Seshadripuram Educational Trust" class="brand-logo" style="border-radius: 50%;">
                <span class="brand-text">Pragmatrix 2026</span>
            </a>
            <div class="header-links">
                <a href="${pageContext.request.contextPath}/admin/dashboard" class="active">Dashboard</a>
                <c:if test="${selectedQuiz == 'VORTEX'}">
                    <a href="${pageContext.request.contextPath}/admin/manage-criteria">Manage Criteria</a>
                </c:if>
                <a href="${pageContext.request.contextPath}/admin/leaderboard?quiz=${selectedQuiz}">Leaderboard</a>
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

        <!-- ===== QUIZ TABS & EXPORT ACTION ===== -->
        <div class="d-flex justify-between align-center flex-wrap gap-md mb-2">
            <div class="quiz-tabs" style="margin-bottom: 0;">
                <a href="${pageContext.request.contextPath}/admin/dashboard?quiz=BIZWIZX"
                   class="quiz-tab <c:if test='${selectedQuiz == "BIZWIZX"}'>active</c:if>"
                   id="tab-bizwizx">BizWizX</a>
                <a href="${pageContext.request.contextPath}/admin/dashboard?quiz=VORTEX"
                   class="quiz-tab <c:if test='${selectedQuiz == "VORTEX"}'>active</c:if>"
                   id="tab-vortex">Vortex</a>
            </div>
            <div>
                <a href="${pageContext.request.contextPath}/admin/export-scores?quiz=${selectedQuiz}"
                   class="btn btn-primary btn-sm" id="btn-export-scores" style="background: linear-gradient(135deg, #107c41, #1e8e3e); border-color: #107c41; display: inline-flex; align-items: center; gap: 0.4rem;"
                   title="Export full scoresheet to Excel (.xlsx)">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                        <polyline points="14 2 14 8 20 8"/>
                        <line x1="8" y1="13" x2="16" y2="13"/>
                        <line x1="8" y1="17" x2="16" y2="17"/>
                        <polyline points="10 9 9 9 8 9"/>
                    </svg>
                    Export Scores (.xlsx)
                </a>
            </div>
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

        <!-- ===== ADD TEAM (Admin-Only Registration) ===== -->
        <div class="glass-panel mb-3">
            <div class="d-flex justify-between align-center">
                <h3 class="section-title mb-0">
                    <span class="title-accent">&#9830;</span> Add Team
                </h3>
                <button type="button" class="btn btn-outline btn-sm" onclick="toggleAddTeam()" id="btn-toggle-add-team">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
                    Register New Team
                </button>
            </div>

            <div id="add-team-form-section" style="display: none; margin-top: 1.25rem;">
                <form action="${pageContext.request.contextPath}/register" method="POST" id="admin-register-form" novalidate>

                    <div class="card-grid" style="grid-template-columns: 1fr 1fr;">
                        <!-- Quiz Selection -->
                        <div class="form-group">
                            <label class="form-label">Select Event <span class="required">*</span></label>
                            <div class="radio-group">
                                <input type="radio" name="quizCode" id="add-quiz-bizwizx" value="BIZWIZX"
                                       <c:if test="${selectedQuiz == 'BIZWIZX'}">checked</c:if>>
                                <label for="add-quiz-bizwizx">BizWizX</label>
                                <input type="radio" name="quizCode" id="add-quiz-vortex" value="VORTEX"
                                       <c:if test="${selectedQuiz == 'VORTEX'}">checked</c:if>>
                                <label for="add-quiz-vortex">Vortex</label>
                            </div>
                        </div>

                        <!-- College Name -->
                        <div class="form-group">
                            <label for="add-collegeName" class="form-label">College Name <span class="required">*</span></label>
                            <input type="text" name="collegeName" id="add-collegeName" class="form-control"
                                   placeholder="e.g. St. Xavier's College" required maxlength="150">
                        </div>
                    </div>

                    <!-- Team Lead Email -->
                    <div class="form-group">
                        <label for="add-leadEmail" class="form-label">Team Lead Email <span class="required">*</span></label>
                        <input type="email" name="leadEmail" id="add-leadEmail" class="form-control"
                               placeholder="teamlead@college.edu" required maxlength="150">
                        <p class="form-hint">Participant ID will be emailed to this address upon team creation</p>
                    </div>

                    <!-- Team Lead Name -->
                        <div class="form-group">
                            <label for="add-teamLeadName" class="form-label">Team Lead Name <span class="required">*</span></label>
                            <input type="text" name="teamLeadName" id="add-teamLeadName" class="form-control"
                                   placeholder="Full name of team lead" required maxlength="100">
                        </div>
                    </div>

                    <button type="submit" class="btn btn-primary" id="btn-submit-add-team">
                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                            <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/>
                            <circle cx="9" cy="7" r="4"/>
                            <line x1="19" y1="8" x2="19" y2="14"/>
                            <line x1="22" y1="11" x2="16" y2="11"/>
                        </svg>
                        Create Team &amp; Send Email
                    </button>
                </form>
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

                            <c:if test="${selectedQuiz == 'VORTEX'}">
                                <a href="${pageContext.request.contextPath}/admin/manage-criteria?roundId=${round.roundNumber}"
                                   class="btn btn-outline btn-sm" id="btn-criteria-${round.roundId}">
                                    Criteria
                                </a>
                            </c:if>

                            <c:if test="${selectedQuiz == 'BIZWIZX' && round.finished && (round.roundNumber == 2 || round.roundNumber == 3)}">
                                <button type="button" class="btn btn-warning btn-sm" onclick="openRankedEliminationModal(${round.roundNumber})" id="btn-eliminate-r${round.roundNumber}" style="background: #ea580c; color: white; border-color: #ea580c; font-weight: 600;">
                                    Standings &amp; Eliminate
                                </button>
                            </c:if>

                            <c:if test="${selectedQuiz == 'VORTEX' && round.finished && round.roundNumber == 3}">
                                <button type="button" class="btn btn-warning btn-sm" onclick="openVortexAdvanceModal()" id="btn-advance-finale" style="background: #7c3aed; color: white; border-color: #6d28d9; font-weight: 600;">
                                    Advance Top 3 to Finale
                                </button>
                            </c:if>

                            <c:if test="${!round.finished}">
                                <button type="button" class="btn btn-outline btn-sm" onclick="toggleEdit(${round.roundId})" id="btn-edit-${round.roundId}">
                                    Edit
                                </button>
                            </c:if>

                            <c:choose>
                                <c:when test="${!round.finished}">
                                    <form action="${pageContext.request.contextPath}/admin/finish-round" method="POST" style="display:inline;" onsubmit="return confirm('Are you sure you want to finish this round? This will lock score editing.');">
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
                            <th>Team Lead</th>
                            <th>Lead Email</th>
                            <th>Total Points</th>
                            <th>Status</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody id="team-tbody">
                        <c:forEach var="team" items="${teams}" varStatus="status">
                            <tr>
                                <td>${status.index + 1}</td>
                                <td><strong style="color: var(--purple-700);"><c:out value="${team.uniqueId}"/></strong></td>
                                <td><c:out value="${team.collegeName}"/></td>
                                <td><c:out value="${team.teamLeadName}"/></td>
                                <td style="font-size: 0.85rem;"><c:out value="${team.leadEmail}"/></td>
                                <td>
                                    <strong style="color: var(--gold-700);">
                                        <c:out value="${team.totalPoints}"/>
                                    </strong>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${selectedQuiz == 'VORTEX'}">
                                            <c:choose>
                                                <c:when test="${team.advancedToFinale}">
                                                    <span class="badge" style="background: rgba(124, 58, 237, 0.15); color: #7c3aed; border: 1px solid rgba(124, 58, 237, 0.3); font-size: 0.75rem; padding: 0.2rem 0.5rem; border-radius: 4px; font-weight: 600;">
                                                        &#11088; Finalist (Top 3)
                                                    </span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge" style="background: rgba(34, 197, 94, 0.15); color: #16a34a; border: 1px solid rgba(34, 197, 94, 0.3); font-size: 0.75rem; padding: 0.2rem 0.5rem; border-radius: 4px; font-weight: 600;">
                                                        Active
                                                    </span>
                                                </c:otherwise>
                                            </c:choose>
                                        </c:when>
                                        <c:otherwise>
                                            <c:choose>
                                                <c:when test="${team.eliminated}">
                                                    <span class="badge" style="background: rgba(239, 68, 68, 0.15); color: #ef4444; border: 1px solid rgba(239, 68, 68, 0.3); font-size: 0.75rem; padding: 0.2rem 0.5rem; border-radius: 4px; font-weight: 600;">
                                                        Eliminated
                                                    </span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge" style="background: rgba(34, 197, 94, 0.15); color: #16a34a; border: 1px solid rgba(34, 197, 94, 0.3); font-size: 0.75rem; padding: 0.2rem 0.5rem; border-radius: 4px; font-weight: 600;">
                                                        Active
                                                    </span>
                                                </c:otherwise>
                                            </c:choose>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <div class="d-flex gap-sm">
                                        <a href="${pageContext.request.contextPath}/admin/scorecard?id=${team.uniqueId}"
                                           class="btn btn-outline btn-sm" title="View Scorecard">
                                            Scorecard
                                        </a>
                                        <c:if test="${selectedQuiz == 'BIZWIZX'}">
                                            <c:choose>
                                                <c:when test="${team.eliminated}">
                                                    <form action="${pageContext.request.contextPath}/admin/eliminate-teams" method="POST" style="display:inline;">
                                                        <input type="hidden" name="uniqueId" value="${team.uniqueId}">
                                                        <input type="hidden" name="quizCode" value="${selectedQuiz}">
                                                        <input type="hidden" name="action" value="restore">
                                                        <button type="submit" class="btn btn-sm btn-outline" title="Restore Team to Active"
                                                                style="padding: 0.3rem 0.6rem; font-size: 0.75rem; color: #16a34a; border-color: #16a34a;"
                                                                onclick="return confirm('Restore team ${team.uniqueId} (${team.collegeName}) to Active?')">
                                                            Restore
                                                        </button>
                                                    </form>
                                                </c:when>
                                                <c:otherwise>
                                                    <form action="${pageContext.request.contextPath}/admin/eliminate-teams" method="POST" style="display:inline;">
                                                        <input type="hidden" name="uniqueId" value="${team.uniqueId}">
                                                        <input type="hidden" name="quizCode" value="${selectedQuiz}">
                                                        <input type="hidden" name="action" value="eliminate">
                                                        <button type="submit" class="btn btn-sm btn-outline" title="Eliminate Team"
                                                                style="padding: 0.3rem 0.6rem; font-size: 0.75rem; color: #ef4444; border-color: #ef4444;"
                                                                onclick="return confirm('Mark team ${team.uniqueId} (${team.collegeName}) as Eliminated? They will no longer be scored in subsequent rounds.')">
                                                            Eliminate
                                                        </button>
                                                    </form>
                                                </c:otherwise>
                                            </c:choose>
                                        </c:if>
                                        <form action="${pageContext.request.contextPath}/admin/resend-email" method="POST" style="display:inline;">
                                            <input type="hidden" name="uniqueId" value="${team.uniqueId}">
                                            <input type="hidden" name="quiz" value="${selectedQuiz}">
                                            <button type="submit" class="btn btn-sm btn-outline" title="Resend ID Email"
                                                    style="padding: 0.3rem 0.6rem; font-size: 0.75rem;"
                                                    onclick="return confirm('Resend participant ID email to this team?')">
                                                Resend
                                            </button>
                                        </form>
                                        <form action="${pageContext.request.contextPath}/admin/delete-team" method="POST" style="display:inline;">
                                            <input type="hidden" name="uniqueId" value="${team.uniqueId}">
                                            <input type="hidden" name="quizCode" value="${selectedQuiz}">
                                            <button type="submit" class="btn btn-danger btn-sm" title="Remove Team"
                                                    style="padding: 0.3rem 0.6rem; font-size: 0.75rem;"
                                                    onclick="return confirm('Are you sure you want to remove team ${team.uniqueId} (${team.collegeName})? This cannot be undone.')">
                                                Remove
                                            </button>
                                        </form>
                                    </div>
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

    <c:if test="${selectedQuiz == 'BIZWIZX'}">
        <!-- Ranked Elimination Modal for BIZWIZX Round 2 -->
        <div id="rankedEliminationModal2" class="modal-overlay" style="display: none; position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0, 0, 0, 0.78); z-index: 9999; align-items: center; justify-content: center; backdrop-filter: blur(5px);">
            <div class="glass-panel" style="max-width: 920px; width: 95%; max-height: 88vh; overflow-y: auto; background: #16192b; border: 1px solid rgba(212, 175, 55, 0.3); border-radius: 12px; padding: 2rem; box-shadow: 0 12px 48px rgba(0,0,0,0.6);">
                <div class="d-flex justify-between align-center mb-2">
                    <div>
                        <h3 style="margin: 0; color: #fff; font-size: 1.35rem; display: flex; align-items: center; gap: 0.5rem;">
                            <span style="color: var(--gold-400);">&#9830;</span> Round 2 Standings &mdash; Eliminate Teams
                        </h3>
                        <p style="margin: 0.35rem 0 0 0; font-size: 0.85rem; color: var(--gray-400);">
                            Active teams ranked by cumulative score (<strong>Round 1 + Round 2</strong>). Teams marked as eliminated will be excluded from subsequent rounds.
                        </p>
                    </div>
                    <button type="button" class="btn btn-outline btn-sm" onclick="closeRankedEliminationModal(2)" style="font-size: 1.3rem; line-height: 1; padding: 0.2rem 0.6rem; color: #fff;">&times;</button>
                </div>

                <c:if test="${hasTieRound2}">
                    <div style="background: rgba(245, 158, 11, 0.15); border: 1px solid rgba(245, 158, 11, 0.4); color: #fbbf24; border-radius: 8px; padding: 0.75rem 1rem; margin-bottom: 1rem; font-size: 0.85rem; display: flex; align-items: center; gap: 0.6rem;">
                        <span style="font-size: 1.1rem;">⚠️</span>
                        <div>
                            <strong>Tie Detected:</strong> Teams with identical cumulative scores are highlighted with a <span class="badge" style="background: #f59e0b; color: #000; font-weight: 700; padding: 0.1rem 0.4rem; border-radius: 3px; font-size: 0.7rem;">TIED</span> badge. Please review tiebreakers carefully before confirming elimination.
                        </div>
                    </div>
                </c:if>

                <form id="rankedEliminationForm2" action="${pageContext.request.contextPath}/admin/eliminate-teams" method="POST">
                    <input type="hidden" name="quizCode" value="BIZWIZX">
                    <input type="hidden" name="action" value="eliminate">

                    <div style="max-height: 380px; overflow-y: auto; border: 1px solid rgba(255,255,255,0.1); border-radius: 8px; margin-bottom: 1.25rem;">
                        <table class="themed-table" style="margin: 0;">
                            <thead>
                                <tr>
                                    <th style="width: 40px; text-align: center;">
                                        <input type="checkbox" id="selectAllR2" onchange="toggleSelectAllR(2, this)" style="cursor: pointer;">
                                    </th>
                                    <th style="width: 75px; text-align: center;">Rank</th>
                                    <th>Unique ID</th>
                                    <th>College</th>
                                    <th>Team Lead</th>
                                    <th style="text-align: center;">R1 Pts</th>
                                    <th style="text-align: center;">R2 Pts</th>
                                    <th style="text-align: center;">Cumulative Total</th>
                                    <th style="text-align: center;">Action</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="entry" items="${round2Standings}" varStatus="status">
                                    <tr style="${entry.tied ? 'background: rgba(245, 158, 11, 0.08);' : ''}">
                                        <td style="text-align: center;">
                                            <input type="checkbox" name="teamIds" value="${entry.uniqueId}" class="team-cb-r2" style="cursor: pointer;">
                                        </td>
                                        <td style="text-align: center;">
                                            <c:choose>
                                                <c:when test="${entry.rank == 1}">
                                                    <span class="badge" style="background: rgba(234, 179, 8, 0.25); color: #facc15; border: 1px solid #facc15; font-weight: 700; padding: 0.2rem 0.5rem; border-radius: 4px;">#1</span>
                                                </c:when>
                                                <c:when test="${entry.rank == 2}">
                                                    <span class="badge" style="background: rgba(203, 213, 225, 0.2); color: #e2e8f0; border: 1px solid #94a3b8; font-weight: 700; padding: 0.2rem 0.5rem; border-radius: 4px;">#2</span>
                                                </c:when>
                                                <c:when test="${entry.rank == 3}">
                                                    <span class="badge" style="background: rgba(217, 119, 6, 0.25); color: #fb923c; border: 1px solid #ea580c; font-weight: 700; padding: 0.2rem 0.5rem; border-radius: 4px;">#3</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span style="font-weight: 600; color: var(--gray-300);">#${entry.rank}</span>
                                                </c:otherwise>
                                            </c:choose>
                                            <c:if test="${entry.tied}">
                                                <span class="badge" style="background: #f59e0b; color: #000; font-size: 0.65rem; font-weight: 700; padding: 0.1rem 0.3rem; border-radius: 3px; margin-left: 0.25rem;">TIED</span>
                                            </c:if>
                                        </td>
                                        <td><strong style="color: var(--purple-700);"><c:out value="${entry.uniqueId}"/></strong></td>
                                        <td><c:out value="${entry.collegeName}"/></td>
                                        <td><c:out value="${entry.teamLeadName}"/></td>
                                        <td style="text-align: center; color: var(--gray-300);">
                                            <c:choose>
                                                <c:when test="${not empty entry.roundPoints[1]}">${entry.roundPoints[1]}</c:when>
                                                <c:otherwise>0</c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td style="text-align: center; color: var(--gray-300);">
                                            <c:choose>
                                                <c:when test="${not empty entry.roundPoints[2]}">${entry.roundPoints[2]}</c:when>
                                                <c:otherwise>0</c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td style="text-align: center;">
                                            <strong style="color: var(--gold-700); font-size: 1.05rem;">
                                                <c:out value="${entry.totalPoints}"/>
                                            </strong>
                                        </td>
                                        <td style="text-align: center;">
                                            <button type="button" class="btn btn-danger btn-sm" style="padding: 0.2rem 0.5rem; font-size: 0.75rem;"
                                                    onclick="eliminateSingleTeam('${entry.uniqueId}', '${entry.collegeName}', '${entry.rank}', '${entry.totalPoints}')">
                                                Eliminate
                                            </button>
                                        </td>
                                    </tr>
                                </c:forEach>
                                <c:if test="${empty round2Standings}">
                                    <tr>
                                        <td colspan="9" style="text-align: center; padding: 2rem; color: var(--gray-400);">
                                            No active teams found for Round 2 standings.
                                        </td>
                                    </tr>
                                </c:if>
                            </tbody>
                        </table>
                    </div>

                    <div class="d-flex justify-between align-center flex-wrap gap-sm">
                        <div class="d-flex gap-sm align-center">
                            <span style="font-size: 0.8rem; color: var(--gray-400);">Quick Select:</span>
                            <button type="button" class="btn btn-outline btn-sm" onclick="selectBottomTeams(2, 1)" style="font-size: 0.75rem; padding: 0.25rem 0.5rem;">Bottom 1</button>
                            <button type="button" class="btn btn-outline btn-sm" onclick="selectBottomTeams(2, 3)" style="font-size: 0.75rem; padding: 0.25rem 0.5rem;">Bottom 3</button>
                            <button type="button" class="btn btn-outline btn-sm" onclick="selectBottomTeams(2, 5)" style="font-size: 0.75rem; padding: 0.25rem 0.5rem;">Bottom 5</button>
                            <button type="button" class="btn btn-outline btn-sm" onclick="clearSelection(2)" style="font-size: 0.75rem; padding: 0.25rem 0.5rem;">Clear</button>
                        </div>
                        <div class="d-flex gap-sm">
                            <button type="button" class="btn btn-danger btn-sm" onclick="submitRankedElimination(2)" style="font-weight: 600;">
                                Eliminate Selected Teams
                            </button>
                            <button type="button" class="btn btn-outline btn-sm" onclick="closeRankedEliminationModal(2)">Close</button>
                        </div>
                    </div>
                </form>
            </div>
        </div>

        <!-- Ranked Elimination Modal for BIZWIZX Round 3 -->
        <div id="rankedEliminationModal3" class="modal-overlay" style="display: none; position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0, 0, 0, 0.78); z-index: 9999; align-items: center; justify-content: center; backdrop-filter: blur(5px);">
            <div class="glass-panel" style="max-width: 950px; width: 95%; max-height: 88vh; overflow-y: auto; background: #16192b; border: 1px solid rgba(212, 175, 55, 0.3); border-radius: 12px; padding: 2rem; box-shadow: 0 12px 48px rgba(0,0,0,0.6);">
                <div class="d-flex justify-between align-center mb-2">
                    <div>
                        <h3 style="margin: 0; color: #fff; font-size: 1.35rem; display: flex; align-items: center; gap: 0.5rem;">
                            <span style="color: var(--gold-400);">&#9830;</span> Round 3 Standings &mdash; Eliminate Teams
                        </h3>
                        <p style="margin: 0.35rem 0 0 0; font-size: 0.85rem; color: var(--gray-400);">
                            Active teams ranked by cumulative score across <strong>Rounds 1 + 2 + 3</strong> (excluding teams already eliminated after Round 2). Select teams to eliminate before Grand Finale.
                        </p>
                    </div>
                    <button type="button" class="btn btn-outline btn-sm" onclick="closeRankedEliminationModal(3)" style="font-size: 1.3rem; line-height: 1; padding: 0.2rem 0.6rem; color: #fff;">&times;</button>
                </div>

                <c:if test="${hasTieRound3}">
                    <div style="background: rgba(245, 158, 11, 0.15); border: 1px solid rgba(245, 158, 11, 0.4); color: #fbbf24; border-radius: 8px; padding: 0.75rem 1rem; margin-bottom: 1rem; font-size: 0.85rem; display: flex; align-items: center; gap: 0.6rem;">
                        <span style="font-size: 1.1rem;">⚠️</span>
                        <div>
                            <strong>Tie Detected:</strong> Teams with identical cumulative scores are highlighted with a <span class="badge" style="background: #f59e0b; color: #000; font-weight: 700; padding: 0.1rem 0.4rem; border-radius: 3px; font-size: 0.7rem;">TIED</span> badge. Please review tiebreakers carefully before confirming elimination.
                        </div>
                    </div>
                </c:if>

                <form id="rankedEliminationForm3" action="${pageContext.request.contextPath}/admin/eliminate-teams" method="POST">
                    <input type="hidden" name="quizCode" value="BIZWIZX">
                    <input type="hidden" name="action" value="eliminate">

                    <div style="max-height: 380px; overflow-y: auto; border: 1px solid rgba(255,255,255,0.1); border-radius: 8px; margin-bottom: 1.25rem;">
                        <table class="themed-table" style="margin: 0;">
                            <thead>
                                <tr>
                                    <th style="width: 40px; text-align: center;">
                                        <input type="checkbox" id="selectAllR3" onchange="toggleSelectAllR(3, this)" style="cursor: pointer;">
                                    </th>
                                    <th style="width: 75px; text-align: center;">Rank</th>
                                    <th>Unique ID</th>
                                    <th>College</th>
                                    <th>Team Lead</th>
                                    <th style="text-align: center;">R1 Pts</th>
                                    <th style="text-align: center;">R2 Pts</th>
                                    <th style="text-align: center;">R3 Pts</th>
                                    <th style="text-align: center;">Cumulative Total</th>
                                    <th style="text-align: center;">Action</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="entry" items="${round3Standings}" varStatus="status">
                                    <tr style="${entry.tied ? 'background: rgba(245, 158, 11, 0.08);' : ''}">
                                        <td style="text-align: center;">
                                            <input type="checkbox" name="teamIds" value="${entry.uniqueId}" class="team-cb-r3" style="cursor: pointer;">
                                        </td>
                                        <td style="text-align: center;">
                                            <c:choose>
                                                <c:when test="${entry.rank == 1}">
                                                    <span class="badge" style="background: rgba(234, 179, 8, 0.25); color: #facc15; border: 1px solid #facc15; font-weight: 700; padding: 0.2rem 0.5rem; border-radius: 4px;">#1</span>
                                                </c:when>
                                                <c:when test="${entry.rank == 2}">
                                                    <span class="badge" style="background: rgba(203, 213, 225, 0.2); color: #e2e8f0; border: 1px solid #94a3b8; font-weight: 700; padding: 0.2rem 0.5rem; border-radius: 4px;">#2</span>
                                                </c:when>
                                                <c:when test="${entry.rank == 3}">
                                                    <span class="badge" style="background: rgba(217, 119, 6, 0.25); color: #fb923c; border: 1px solid #ea580c; font-weight: 700; padding: 0.2rem 0.5rem; border-radius: 4px;">#3</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span style="font-weight: 600; color: var(--gray-300);">#${entry.rank}</span>
                                                </c:otherwise>
                                            </c:choose>
                                            <c:if test="${entry.tied}">
                                                <span class="badge" style="background: #f59e0b; color: #000; font-size: 0.65rem; font-weight: 700; padding: 0.1rem 0.3rem; border-radius: 3px; margin-left: 0.25rem;">TIED</span>
                                            </c:if>
                                        </td>
                                        <td><strong style="color: var(--purple-700);"><c:out value="${entry.uniqueId}"/></strong></td>
                                        <td><c:out value="${entry.collegeName}"/></td>
                                        <td><c:out value="${entry.teamLeadName}"/></td>
                                        <td style="text-align: center; color: var(--gray-300);">
                                            <c:choose>
                                                <c:when test="${not empty entry.roundPoints[1]}">${entry.roundPoints[1]}</c:when>
                                                <c:otherwise>0</c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td style="text-align: center; color: var(--gray-300);">
                                            <c:choose>
                                                <c:when test="${not empty entry.roundPoints[2]}">${entry.roundPoints[2]}</c:when>
                                                <c:otherwise>0</c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td style="text-align: center; color: var(--gray-300);">
                                            <c:choose>
                                                <c:when test="${not empty entry.roundPoints[3]}">${entry.roundPoints[3]}</c:when>
                                                <c:otherwise>0</c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td style="text-align: center;">
                                            <strong style="color: var(--gold-700); font-size: 1.05rem;">
                                                <c:out value="${entry.totalPoints}"/>
                                            </strong>
                                        </td>
                                        <td style="text-align: center;">
                                            <button type="button" class="btn btn-danger btn-sm" style="padding: 0.2rem 0.5rem; font-size: 0.75rem;"
                                                    onclick="eliminateSingleTeam('${entry.uniqueId}', '${entry.collegeName}', '${entry.rank}', '${entry.totalPoints}')">
                                                Eliminate
                                            </button>
                                        </td>
                                    </tr>
                                </c:forEach>
                                <c:if test="${empty round3Standings}">
                                    <tr>
                                        <td colspan="10" style="text-align: center; padding: 2rem; color: var(--gray-400);">
                                            No active teams found for Round 3 standings.
                                        </td>
                                    </tr>
                                </c:if>
                            </tbody>
                        </table>
                    </div>

                    <div class="d-flex justify-between align-center flex-wrap gap-sm">
                        <div class="d-flex gap-sm align-center">
                            <span style="font-size: 0.8rem; color: var(--gray-400);">Quick Select:</span>
                            <button type="button" class="btn btn-outline btn-sm" onclick="selectBottomTeams(3, 1)" style="font-size: 0.75rem; padding: 0.25rem 0.5rem;">Bottom 1</button>
                            <button type="button" class="btn btn-outline btn-sm" onclick="selectBottomTeams(3, 3)" style="font-size: 0.75rem; padding: 0.25rem 0.5rem;">Bottom 3</button>
                            <button type="button" class="btn btn-outline btn-sm" onclick="selectBottomTeams(3, 5)" style="font-size: 0.75rem; padding: 0.25rem 0.5rem;">Bottom 5</button>
                            <button type="button" class="btn btn-outline btn-sm" onclick="clearSelection(3)" style="font-size: 0.75rem; padding: 0.25rem 0.5rem;">Clear</button>
                        </div>
                        <div class="d-flex gap-sm">
                            <button type="button" class="btn btn-danger btn-sm" onclick="submitRankedElimination(3)" style="font-weight: 600;">
                                Eliminate Selected Teams
                            </button>
                            <button type="button" class="btn btn-outline btn-sm" onclick="closeRankedEliminationModal(3)">Close</button>
                        </div>
                    </div>
                </form>
            </div>
        </div>
    </c:if>

    <!-- ===== VORTEX FINALE ADVANCEMENT MODAL ===== -->
    <c:if test="${selectedQuiz == 'VORTEX' && not empty vortexFinaleStandings}">
        <div id="vortexAdvanceModal" class="modal-backdrop" style="display: none; position: fixed; top: 0; left: 0; width: 100vw; height: 100vh; background: rgba(0,0,0,0.7); z-index: 9999; align-items: center; justify-content: center; backdrop-filter: blur(4px);">
            <div class="glass-panel" style="max-width: 950px; width: 95%; max-height: 90vh; overflow-y: auto; background: var(--bg-card); border: 1px solid var(--gold-500); box-shadow: 0 10px 30px rgba(0,0,0,0.5); padding: 1.75rem; border-radius: 12px;">
                
                <div class="d-flex justify-between align-center mb-2" style="border-bottom: 1px solid rgba(212,175,55,0.2); padding-bottom: 0.75rem;">
                    <div>
                        <h3 class="section-title mb-0" style="color: var(--gold-600); font-size: 1.25rem;">
                            <span class="title-accent">&#11088;</span> VORTEX &mdash; Advance Top 3 Teams to GRAND FINALE
                        </h3>
                        <p style="margin: 0.25rem 0 0 0; color: var(--gray-600); font-size: 0.85rem;">
                            Cumulative standings across <strong>KAIROS (R1)</strong>, <strong>TREORAI (R2)</strong>, and <strong>ENMA (R3)</strong>. Select the 3 finalist teams to advance to <strong>GRAND FINALE</strong>.
                        </p>
                    </div>
                    <button type="button" class="btn btn-outline btn-sm" onclick="closeVortexAdvanceModal()" style="padding: 0.25rem 0.6rem;">&times;</button>
                </div>

                <c:if test="${hasTieAtCutoffVortex}">
                    <div class="alert alert-warning" style="margin-bottom: 1.25rem; background: rgba(245, 158, 11, 0.12); border: 1px solid rgba(245, 158, 11, 0.4); color: #d97706; display: flex; align-items: flex-start; gap: 0.75rem; border-radius: 8px; padding: 0.85rem 1rem;">
                        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="flex-shrink:0; margin-top:2px;"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>
                        <div>
                            <strong style="font-size: 0.95rem;">&#9888; Tie Detected at 3rd Place Cutoff!</strong>
                            <p style="margin: 0.25rem 0 0 0; font-size: 0.85rem; line-height: 1.4;">
                                Multiple teams share identical scores at the 3rd place qualifying boundary. The system will NOT automatically guess which team advances. Please review the round scores and manually check the 3 teams you wish to qualify.
                            </p>
                        </div>
                    </div>
                </c:if>

                <form id="vortexAdvanceForm" action="${pageContext.request.contextPath}/admin/advance-finale" method="POST">
                    <input type="hidden" name="quizCode" value="VORTEX">

                    <div class="table-wrapper mb-2" style="max-height: 420px; overflow-y: auto;">
                        <table class="themed-table" style="font-size: 0.9rem;">
                            <thead>
                                <tr>
                                    <th style="width: 40px; text-align: center;">
                                        <input type="checkbox" id="selectAllVortexAdvance" onchange="toggleSelectAllVortex(this)" title="Select/Deselect All">
                                    </th>
                                    <th style="width: 60px;">Rank</th>
                                    <th>Unique ID</th>
                                    <th>College Name</th>
                                    <th>Team Lead</th>
                                    <th style="text-align: right;">Kairos (R1)</th>
                                    <th style="text-align: right;">Treorai (R2)</th>
                                    <th style="text-align: right;">Enma (R3)</th>
                                    <th style="text-align: right;">Cumulative</th>
                                    <th style="text-align: center;">Status</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="entry" items="${vortexFinaleStandings}" varStatus="status">
                                    <tr style="${entry.advanced ? 'background: rgba(124, 58, 237, 0.08);' : (status.index < 3 ? 'background: rgba(212, 175, 55, 0.05);' : '')}">
                                        <td style="text-align: center;">
                                            <input type="checkbox" name="teamIds" value="${entry.uniqueId}"
                                                   class="vortex-advance-cb"
                                                   data-index="${status.index}"
                                                   data-rank="${entry.rank}"
                                                   <c:if test="${entry.advanced || (status.index < 3 && !hasTieAtCutoffVortex)}">checked</c:if>>
                                        </td>
                                        <td>
                                            <strong style="color: ${status.index < 3 ? 'var(--gold-600)' : 'var(--gray-600)'}; font-size: 0.95rem;">
                                                #${entry.rank}
                                            </strong>
                                            <c:if test="${entry.tied}">
                                                <span class="badge" style="background: rgba(245, 158, 11, 0.15); color: #d97706; font-size: 0.65rem; padding: 0.1rem 0.3rem; margin-left: 2px;">TIED</span>
                                            </c:if>
                                        </td>
                                        <td><strong style="color: var(--purple-700);"><c:out value="${entry.uniqueId}"/></strong></td>
                                        <td><c:out value="${entry.collegeName}"/></td>
                                        <td><c:out value="${entry.teamLeadName}"/></td>
                                        <td style="text-align: right; color: var(--gray-600);">
                                            <c:choose>
                                                <c:when test="${entry.roundPoints[1] != null}"><fmt:formatNumber value="${entry.roundPoints[1]}" minFractionDigits="0" maxFractionDigits="2"/></c:when>
                                                <c:otherwise>&mdash;</c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td style="text-align: right; color: var(--gray-600);">
                                            <c:choose>
                                                <c:when test="${entry.roundPoints[2] != null}"><fmt:formatNumber value="${entry.roundPoints[2]}" minFractionDigits="0" maxFractionDigits="2"/></c:when>
                                                <c:otherwise>&mdash;</c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td style="text-align: right; color: var(--gray-600);">
                                            <c:choose>
                                                <c:when test="${entry.roundPoints[3] != null}"><fmt:formatNumber value="${entry.roundPoints[3]}" minFractionDigits="0" maxFractionDigits="2"/></c:when>
                                                <c:otherwise>&mdash;</c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td style="text-align: right;">
                                            <strong style="color: var(--gold-700); font-size: 1rem;">
                                                <fmt:formatNumber value="${entry.totalPoints}" minFractionDigits="0" maxFractionDigits="2"/>
                                            </strong>
                                        </td>
                                        <td style="text-align: center;">
                                            <c:choose>
                                                <c:when test="${entry.advanced}">
                                                    <span class="badge" style="background: rgba(124, 58, 237, 0.15); color: #7c3aed; font-size: 0.75rem; padding: 0.2rem 0.5rem; border-radius: 4px; font-weight: 600;">
                                                        Finalist
                                                    </span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge" style="background: rgba(107, 114, 128, 0.1); color: var(--gray-500); font-size: 0.75rem; padding: 0.2rem 0.5rem; border-radius: 4px;">
                                                        Not Selected
                                                    </span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                    </tr>
                                </c:forEach>
                                <c:if test="${empty vortexFinaleStandings}">
                                    <tr>
                                        <td colspan="10" style="text-align: center; padding: 2rem; color: var(--gray-400);">
                                            No teams or scores recorded for VORTEX rounds 1 to 3 yet.
                                        </td>
                                    </tr>
                                </c:if>
                            </tbody>
                        </table>
                    </div>

                    <div class="d-flex justify-between align-center flex-wrap gap-sm">
                        <div class="d-flex gap-sm align-center">
                            <span style="font-size: 0.8rem; color: var(--gray-400);">Quick Select:</span>
                            <button type="button" class="btn btn-outline btn-sm" onclick="selectTopVortex(3)" style="font-size: 0.75rem; padding: 0.25rem 0.5rem;">Top 3</button>
                            <button type="button" class="btn btn-outline btn-sm" onclick="clearVortexAdvanceSelection()" style="font-size: 0.75rem; padding: 0.25rem 0.5rem;">Clear All</button>
                        </div>
                        <div class="d-flex gap-sm">
                            <button type="button" class="btn btn-primary btn-sm" onclick="submitVortexAdvance()" style="font-weight: 600; background: #7c3aed; border-color: #6d28d9;">
                                Confirm &amp; Advance Finalists
                            </button>
                            <button type="button" class="btn btn-outline btn-sm" onclick="closeVortexAdvanceModal()">Cancel</button>
                        </div>
                    </div>
                </form>
            </div>
        </div>
    </c:if>

    <!-- Footer -->
    <footer class="site-footer">
        <span class="footer-brand">PRAGMATRIX 2026</span> &mdash; Admin Dashboard
    </footer>

    <script>
    /* VORTEX Advance Modal Handlers */
    function openVortexAdvanceModal() {
        var modal = document.getElementById('vortexAdvanceModal');
        if (modal) {
            modal.style.display = 'flex';
        }
    }

    function closeVortexAdvanceModal() {
        var modal = document.getElementById('vortexAdvanceModal');
        if (modal) {
            modal.style.display = 'none';
        }
    }

    function toggleSelectAllVortex(masterCheckbox) {
        var checkboxes = document.querySelectorAll('.vortex-advance-cb');
        checkboxes.forEach(function(cb) {
            cb.checked = masterCheckbox.checked;
        });
    }

    function clearVortexAdvanceSelection() {
        var checkboxes = document.querySelectorAll('.vortex-advance-cb');
        checkboxes.forEach(function(cb) {
            cb.checked = false;
        });
        var master = document.getElementById('selectAllVortexAdvance');
        if (master) master.checked = false;
    }

    function selectTopVortex(count) {
        clearVortexAdvanceSelection();
        var checkboxes = Array.from(document.querySelectorAll('.vortex-advance-cb'));
        for (var i = 0; i < Math.min(count, checkboxes.length); i++) {
            checkboxes[i].checked = true;
        }
    }

    function submitVortexAdvance() {
        var checked = document.querySelectorAll('.vortex-advance-cb:checked');
        if (checked.length === 0) {
            if (!confirm('No teams are selected. This will reset the GRAND FINALE finalist list. Continue?')) {
                return;
            }
        } else {
            var msg = 'Are you sure you want to advance ' + checked.length + ' team(s) to the GRAND FINALE?\\n\\n';
            if (checked.length !== 3) {
                msg += 'Note: The standard number of finalists is 3. You currently have ' + checked.length + ' selected.\\n\\n';
            }
            msg += 'Only selected teams will be scored in GRAND FINALE.';
            if (!confirm(msg)) {
                return;
            }
        }
        document.getElementById('vortexAdvanceForm').submit();
    }

    /* Ranked Elimination Modal Handlers */
    function openRankedEliminationModal(roundNum) {
        var modal = document.getElementById('rankedEliminationModal' + roundNum);
        if (modal) {
            modal.style.display = 'flex';
        }
    }

    function closeRankedEliminationModal(roundNum) {
        var modal = document.getElementById('rankedEliminationModal' + roundNum);
        if (modal) {
            modal.style.display = 'none';
        }
    }

    function toggleSelectAllR(roundNum, masterCheckbox) {
        var checkboxes = document.querySelectorAll('.team-cb-r' + roundNum);
        checkboxes.forEach(function(cb) {
            cb.checked = masterCheckbox.checked;
        });
    }

    function clearSelection(roundNum) {
        var checkboxes = document.querySelectorAll('.team-cb-r' + roundNum);
        checkboxes.forEach(function(cb) {
            cb.checked = false;
        });
        var master = document.getElementById('selectAllR' + roundNum);
        if (master) master.checked = false;
    }

    function selectBottomTeams(roundNum, count) {
        clearSelection(roundNum);
        var checkboxes = Array.from(document.querySelectorAll('.team-cb-r' + roundNum));
        var total = checkboxes.length;
        var startIdx = Math.max(0, total - count);
        for (var i = startIdx; i < total; i++) {
            checkboxes[i].checked = true;
        }
    }

    function submitRankedElimination(roundNum) {
        var checked = document.querySelectorAll('.team-cb-r' + roundNum + ':checked');
        if (checked.length === 0) {
            alert('Please select at least one team to eliminate.');
            return;
        }
        if (!confirm('Are you sure you want to ELIMINATE ' + checked.length + ' selected team(s)? They will no longer appear for scoring in subsequent rounds.')) {
            return;
        }
        document.getElementById('rankedEliminationForm' + roundNum).submit();
    }

    function eliminateSingleTeam(uniqueId, collegeName, rank, points) {
        if (!confirm('Eliminate team ' + uniqueId + ' (' + collegeName + ', Rank #' + rank + ', ' + points + ' pts)?\n\nThey will be excluded from subsequent rounds.')) {
            return;
        }
        var form = document.createElement('form');
        form.method = 'POST';
        form.action = '${pageContext.request.contextPath}/admin/eliminate-teams';
        
        var uidInput = document.createElement('input');
        uidInput.type = 'hidden';
        uidInput.name = 'uniqueId';
        uidInput.value = uniqueId;
        form.appendChild(uidInput);

        var quizInput = document.createElement('input');
        quizInput.type = 'hidden';
        quizInput.name = 'quizCode';
        quizInput.value = 'BIZWIZX';
        form.appendChild(quizInput);

        var actionInput = document.createElement('input');
        actionInput.type = 'hidden';
        actionInput.name = 'action';
        actionInput.value = 'eliminate';
        form.appendChild(actionInput);

        document.body.appendChild(form);
        form.submit();
    }

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

    /* Toggle Add Team form */
    function toggleAddTeam() {
        var section = document.getElementById('add-team-form-section');
        if (section) {
            section.style.display = section.style.display === 'none' ? 'block' : 'none';
        }
    }

    /* Live search with debounce */
    (function() {
        var searchInput = document.getElementById('team-search');
        var timer;

        if (searchInput) {
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
        }
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
