<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Edit Team — <c:out value="${team.uniqueId}"/> — PRAGMATRIX 2026</title>
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
                <a href="${pageContext.request.contextPath}/admin/dashboard?quiz=${team.quizCode}">Dashboard</a>
                <c:if test="${team.quizCode == 'VORTEX'}">
                    <a href="${pageContext.request.contextPath}/admin/manage-criteria">Manage Criteria</a>
                </c:if>
                <a href="${pageContext.request.contextPath}/admin/leaderboard?quiz=${team.quizCode}">Leaderboard</a>
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
    <div class="page-container" style="max-width: 800px;">

        <!-- Back Link -->
        <div class="d-flex justify-between align-center flex-wrap gap-md mb-2">
            <div>
                <h1 class="page-title" style="margin-bottom: 0.25rem;">Edit Team Details</h1>
                <p class="page-subtitle" style="margin-bottom: 0;">Update registration and profile information for this team</p>
            </div>
            <a href="${pageContext.request.contextPath}/admin/dashboard?quiz=${team.quizCode}" class="btn btn-outline btn-sm">
                &larr; Back to Dashboard
            </a>
        </div>

        <!-- Error Alert -->
        <c:if test="${not empty errorMessage}">
            <div class="alert alert-error" style="margin-bottom: 1.5rem;">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/>
                </svg>
                <c:out value="${errorMessage}"/>
            </div>
        </c:if>

        <!-- Edit Form Card -->
        <div class="glass-panel" style="padding: 2rem;">

            <!-- Team Summary Banner (Read-Only Context) -->
            <div style="background: rgba(124, 58, 237, 0.06); border: 1px solid rgba(124, 58, 237, 0.2); border-radius: var(--radius-md); padding: 1.25rem; margin-bottom: 1.75rem;">
                <div class="d-flex justify-between align-center flex-wrap gap-md">
                    <div>
                        <div style="font-size: 0.75rem; font-weight: 700; color: var(--purple-600); text-transform: uppercase; letter-spacing: 1px; margin-bottom: 0.25rem;">
                            Team Identifier (Read-Only)
                        </div>
                        <div style="font-family: var(--font-display); font-size: 1.6rem; font-weight: 800; color: var(--purple-800); letter-spacing: 1.5px;">
                            <c:out value="${team.uniqueId}"/>
                        </div>
                    </div>
                    <div class="d-flex align-center gap-sm">
                        <span class="badge" style="background: var(--purple-100); color: var(--purple-700); font-weight: 700; font-size: 0.85rem; padding: 0.35rem 0.75rem; border-radius: 4px;">
                            Event: <c:out value="${team.quizCode}"/>
                        </span>
                        <c:if test="${team.eliminated}">
                            <span class="badge" style="background: rgba(239, 68, 68, 0.15); color: #ef4444; border: 1px solid rgba(239, 68, 68, 0.3); font-size: 0.85rem; padding: 0.35rem 0.75rem; border-radius: 4px; font-weight: 600;">
                                Eliminated
                            </span>
                        </c:if>
                        <c:if test="${team.advancedToFinale}">
                            <span class="badge" style="background: rgba(124, 58, 237, 0.15); color: #7c3aed; border: 1px solid rgba(124, 58, 237, 0.3); font-size: 0.85rem; padding: 0.35rem 0.75rem; border-radius: 4px; font-weight: 700;">
                                &#11088; Grand Finale Finalist
                            </span>
                        </c:if>
                    </div>
                </div>
            </div>

            <!-- Form -->
            <form action="${pageContext.request.contextPath}/admin/edit-team" method="POST" id="edit-team-form">
                <!-- Hidden immutable identifier -->
                <input type="hidden" name="uniqueId" value="<c:out value='${team.uniqueId}'/>">

                <!-- Team Code / Unique ID (Disabled Display) -->
                <div class="form-group">
                    <label class="form-label">Team Code / Unique ID <span style="font-size: 0.8rem; font-weight: normal; color: var(--gray-400);">(System generated, immutable)</span></label>
                    <input type="text" class="form-control" value="<c:out value='${team.uniqueId}'/>" readonly disabled style="background: var(--gray-100); color: var(--gray-600); cursor: not-allowed; font-weight: 600; font-family: var(--font-display);">
                </div>

                <!-- College Name -->
                <div class="form-group">
                    <label for="collegeName" class="form-label">College Name <span class="required">*</span></label>
                    <input type="text" name="collegeName" id="collegeName" class="form-control"
                           placeholder="e.g. St. Xavier's College"
                           value="<c:out value='${team.collegeName}'/>"
                           required maxlength="150">
                </div>

                <!-- Team Lead Name -->
                <div class="form-group">
                    <label for="teamLeadName" class="form-label">Team Lead Name <span class="required">*</span></label>
                    <input type="text" name="teamLeadName" id="teamLeadName" class="form-control"
                           placeholder="Full name of team lead"
                           value="<c:out value='${teamLeadName != null ? teamLeadName : team.teamLeadName}'/>"
                           required maxlength="100">
                </div>

                <!-- Team Lead Email -->
                <div class="form-group">
                    <label for="leadEmail" class="form-label">Team Lead Email <span class="required">*</span></label>
                    <input type="email" name="leadEmail" id="leadEmail" class="form-control"
                           placeholder="teamlead@college.edu"
                           value="<c:out value='${leadEmail != null ? leadEmail : team.leadEmail}'/>"
                           required maxlength="150">
                    <p class="form-hint">Used for login OTP and system notifications</p>
                </div>

                <div class="card-grid" style="grid-template-columns: 1fr 1fr; gap: 1.25rem;">
                    <!-- Member 2 Name (Optional) -->
                    <div class="form-group">
                        <label for="member2Name" class="form-label">Member 2 Name <span style="font-size: 0.85rem; font-weight: normal; color: var(--gray-400);">(Optional)</span></label>
                        <input type="text" name="member2Name" id="member2Name" class="form-control"
                               placeholder="Full name of member 2"
                               value="<c:out value='${member2Name != null ? member2Name : team.member2Name}'/>"
                               maxlength="150">
                    </div>

                    <!-- Member 3 Name (Optional) -->
                    <div class="form-group">
                        <label for="member3Name" class="form-label">Member 3 Name <span style="font-size: 0.85rem; font-weight: normal; color: var(--gray-400);">(Optional)</span></label>
                        <input type="text" name="member3Name" id="member3Name" class="form-control"
                               placeholder="Full name of member 3"
                               value="<c:out value='${member3Name != null ? member3Name : team.member3Name}'/>"
                               maxlength="150">
                    </div>
                </div>

                <div class="gold-divider" style="margin: 1.75rem 0;"></div>

                <!-- Form Action Buttons -->
                <div class="d-flex justify-between align-center flex-wrap gap-md">
                    <a href="${pageContext.request.contextPath}/admin/dashboard?quiz=${team.quizCode}" class="btn btn-outline" id="btn-cancel-edit">
                        Cancel
                    </a>
                    <button type="submit" class="btn btn-primary btn-lg" id="btn-save-team">
                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <path d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z"/>
                            <polyline points="17 21 17 13 7 13 7 21"/>
                            <polyline points="7 3 7 8 15 8"/>
                        </svg>
                        Save Changes
                    </button>
                </div>
            </form>

        </div>

    </div>

    <!-- Auto-dismiss alerts script -->
    <script>
    setTimeout(function() {
        var alerts = document.querySelectorAll('.alert');
        alerts.forEach(function(a) {
            a.style.transition = 'opacity 0.5s ease';
            a.style.opacity = '0';
            setTimeout(function() { a.remove(); }, 500);
        });
    }, 6000);
    </script>
</body>
</html>
