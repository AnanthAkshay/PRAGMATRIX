<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Manage Judging Criteria — PRAGMATRIX 2026</title>
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
                <a href="${pageContext.request.contextPath}/admin/dashboard">Dashboard</a>
                <a href="${pageContext.request.contextPath}/admin/manage-criteria" class="active">Manage Criteria</a>
                <a href="${pageContext.request.contextPath}/admin/leaderboard?quiz=VORTEX">Leaderboard</a>
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

        <div class="d-flex justify-between align-center flex-wrap gap-md mb-2">
            <div>
                <h1 class="page-title">VORTEX Judging Criteria</h1>
                <p class="page-subtitle">Configure round structure, components, criteria, and max marks</p>
            </div>
            <a href="${pageContext.request.contextPath}/admin/dashboard?quiz=VORTEX" class="btn btn-outline btn-sm">
                &larr; Back to Dashboard
            </a>
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

        <!-- Round Tabs -->
        <div class="quiz-tabs" style="margin-bottom: 1.5rem;">
            <c:forEach var="r" items="${vortexRounds}">
                <a href="${pageContext.request.contextPath}/admin/manage-criteria?roundId=${r.roundId}"
                   class="quiz-tab <c:if test='${r.roundId == currentRound.roundId}'>active</c:if>">
                    Round ${r.displayOrder}: <c:out value="${r.roundName}"/>
                </a>
            </c:forEach>
        </div>

        <c:if test="${not empty currentRound}">
            <!-- Round Overview Panel -->
            <div class="glass-panel mb-3">
                <div class="d-flex justify-between align-center flex-wrap gap-md">
                    <div>
                        <h2 style="font-family: var(--font-display); color: var(--gold-700); margin: 0;">
                            <c:out value="${currentRound.roundName}"/> Structure
                        </h2>
                        <p style="color: var(--gray-600); margin: 0.25rem 0 0 0; font-size: 0.9rem;">
                            Total Components: <strong>${currentRound.components.size()}</strong> | Total Max Marks: <strong>${currentRound.totalMaxMarks} Marks</strong>
                        </p>
                    </div>
                    <button type="button" class="btn btn-primary btn-sm" onclick="toggleAddComp()">
                        + Add Component
                    </button>
                </div>

                <!-- Add Component Form (Hidden) -->
                <div id="add-comp-form" style="display: none; margin-top: 1rem; padding-top: 1rem; border-top: 1px solid rgba(212, 175, 55, 0.2);">
                    <form action="${pageContext.request.contextPath}/admin/manage-criteria" method="POST" class="d-flex gap-sm align-end flex-wrap">
                        <input type="hidden" name="action" value="addComponent">
                        <input type="hidden" name="roundId" value="${currentRound.roundId}">
                        <div class="form-group mb-0" style="flex: 1; min-width: 200px;">
                            <label class="form-label">Component Title / Label</label>
                            <input type="text" name="componentLabel" class="form-control" placeholder="e.g. Component A — Pitch" required>
                        </div>
                        <div class="form-group mb-0" style="width: 100px;">
                            <label class="form-label">Order</label>
                            <input type="number" name="displayOrder" class="form-control" value="${currentRound.components.size() + 1}" required>
                        </div>
                        <button type="submit" class="btn btn-primary btn-sm">Save Component</button>
                    </form>
                </div>
            </div>

            <!-- Components & Criteria List -->
            <c:forEach var="comp" items="${currentRound.components}" varStatus="compStatus">
                <div class="glass-panel mb-3">
                    <div class="d-flex justify-between align-center flex-wrap gap-md" style="padding-bottom: 0.75rem; border-bottom: 1px solid rgba(212,175,55,0.2);">
                        <div>
                            <h3 class="section-title mb-0">
                                <span class="title-accent">&#9830;</span>
                                <c:out value="${comp.componentLabel}"/>
                            </h3>
                            <span class="badge" style="background: rgba(147,51,234,0.1); color: var(--purple-700); font-weight: 600;">
                                Component Subtotal: ${comp.maxMarks} Marks
                            </span>
                        </div>
                        <div class="d-flex gap-sm">
                            <button type="button" class="btn btn-outline btn-sm" onclick="toggleAddCrit(${comp.componentId})">
                                + Add Criterion
                            </button>
                            <form action="${pageContext.request.contextPath}/admin/manage-criteria" method="POST" style="display:inline;" onsubmit="return confirm('Delete this component and all its criteria?')">
                                <input type="hidden" name="action" value="deleteComponent">
                                <input type="hidden" name="roundId" value="${currentRound.roundId}">
                                <input type="hidden" name="componentId" value="${comp.componentId}">
                                <button type="submit" class="btn btn-danger btn-sm">Delete</button>
                            </form>
                        </div>
                    </div>

                    <!-- Add Criterion Form -->
                    <div id="add-crit-form-${comp.componentId}" style="display: none; margin: 1rem 0; padding: 1rem; background: rgba(255,255,255,0.03); border-radius: 8px;">
                        <h4 style="margin-top: 0; color: var(--gold-600); font-size: 0.95rem;">New Criterion for ${comp.componentLabel}</h4>
                        <form action="${pageContext.request.contextPath}/admin/manage-criteria" method="POST">
                            <input type="hidden" name="action" value="addCriterion">
                            <input type="hidden" name="roundId" value="${currentRound.roundId}">
                            <input type="hidden" name="componentId" value="${comp.componentId}">

                            <div class="card-grid" style="grid-template-columns: 2fr 2fr 1fr; margin-bottom: 0.75rem;">
                                <div class="form-group mb-0">
                                    <label class="form-label">Criterion Name <span class="required">*</span></label>
                                    <input type="text" name="criterionName" class="form-control" placeholder="e.g. Crisis Diagnosis" required>
                                </div>
                                <div class="form-group mb-0">
                                    <label class="form-label">Judges Look For (Optional)</label>
                                    <input type="text" name="judgesLookFor" class="form-control" placeholder="e.g. All 4 covered, consistent">
                                </div>
                                <div class="form-group mb-0">
                                    <label class="form-label">Max Marks <span class="required">*</span></label>
                                    <input type="number" name="maxMarks" class="form-control" placeholder="15" min="1" max="100" required>
                                </div>
                            </div>
                            <div class="d-flex justify-end gap-sm">
                                <button type="button" class="btn btn-outline btn-sm" onclick="toggleAddCrit(${comp.componentId})">Cancel</button>
                                <button type="submit" class="btn btn-primary btn-sm">Save Criterion</button>
                            </div>
                        </form>
                    </div>

                    <!-- Criteria Table -->
                    <div class="table-wrapper" style="margin-top: 1rem;">
                        <table class="themed-table">
                            <thead>
                                <tr>
                                    <th style="width: 50px;">#</th>
                                    <th>Criterion</th>
                                    <th>Judges Look For</th>
                                    <th style="width: 100px; text-align: center;">Max Marks</th>
                                    <th style="width: 120px; text-align: center;">Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="crit" items="${comp.criteria}" varStatus="critStatus">
                                    <tr>
                                        <td>${critStatus.index + 1}</td>
                                        <td><strong><c:out value="${crit.criterionName}"/></strong></td>
                                        <td style="color: var(--gray-600); font-style: italic;">
                                            <c:choose>
                                                <c:when test="${not empty crit.judgesLookFor}">
                                                    &ldquo;<c:out value="${crit.judgesLookFor}"/>&rdquo;
                                                </c:when>
                                                <c:otherwise>&mdash;</c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td style="text-align: center;"><strong style="color: var(--gold-700);">${crit.maxMarks}</strong></td>
                                        <td style="text-align: center;">
                                            <form action="${pageContext.request.contextPath}/admin/manage-criteria" method="POST" style="display:inline;" class="delete-crit-form">
                                                <input type="hidden" name="action" value="deleteCriterion">
                                                <input type="hidden" name="roundId" value="${currentRound.roundId}">
                                                <input type="hidden" name="criterionId" value="${crit.criterionId}">
                                                <button type="submit" class="btn btn-danger btn-sm" style="padding: 0.25rem 0.5rem; font-size: 0.75rem;">Delete</button>
                                            </form>
                                        </td>
                                    </tr>
                                </c:forEach>
                                <c:if test="${empty comp.criteria}">
                                    <tr>
                                        <td colspan="5" style="text-align: center; color: var(--gray-500); padding: 1.5rem;">
                                            No criteria added to this component yet. Click "+ Add Criterion" above to build criteria.
                                        </td>
                                    </tr>
                                </c:if>
                            </tbody>
                        </table>
                    </div>
                </div>
            </c:forEach>

            <c:if test="${empty currentRound.components}">
                <div class="glass-panel text-center" style="padding: 3rem;">
                    <h3 style="color: var(--gray-500);">No components created for <c:out value="${currentRound.roundName}"/> yet.</h3>
                    <p style="color: var(--gray-400);">Click "+ Add Component" above to build the structure for this round.</p>
                </div>
            </c:if>
        </c:if>

    </div>

    <!-- Footer -->
    <footer class="site-footer">
        <span class="footer-brand">PRAGMATRIX 2026</span> &mdash; Admin Management
    </footer>

    <script>
    function toggleAddComp() {
        var el = document.getElementById('add-comp-form');
        if (el) el.style.display = (el.style.display === 'none') ? 'block' : 'none';
    }

    function toggleAddCrit(compId) {
        var el = document.getElementById('add-crit-form-' + compId);
        if (el) el.style.display = (el.style.display === 'none') ? 'block' : 'none';
    }

    // Confirm before deleting criterion
    document.querySelectorAll('.delete-crit-form').forEach(function(form) {
        form.addEventListener('submit', function(e) {
            if (!confirm('Are you sure you want to delete this criterion?')) {
                e.preventDefault();
            }
        });
    });
    </script>
</body>
</html>
