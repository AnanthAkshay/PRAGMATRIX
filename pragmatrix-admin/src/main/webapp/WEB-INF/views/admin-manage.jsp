<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Admin Management — PRAGMATRIX 2026</title>
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
                <a href="${pageContext.request.contextPath}/admin/dashboard">Dashboard</a>
                <a href="${pageContext.request.contextPath}/admin/leaderboard?quiz=BIZWIZX">Leaderboard</a>
                <a href="${pageContext.request.contextPath}/admin/admins" class="active">Admins</a>
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
        <h1 class="page-title">Admin Management</h1>
        <p class="page-subtitle">Manage administrator accounts with secure hashed credentials (Cap: 10 accounts)</p>

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

        <!-- ===== STATS ROW ===== -->
        <div class="card-grid" style="grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); margin-bottom: 1.5rem;">
            <div class="stat-card">
                <div class="stat-value" id="stat-admin-count" style="color: var(--gold-400);">
                    <c:out value="${adminCount}"/> / <c:out value="${maxAdmins}"/>
                </div>
                <div class="stat-label">Active Admin Accounts</div>
            </div>
            <div class="stat-card">
                <div class="stat-value" style="font-size: 1.2rem; padding-top: 0.4rem;">
                    <c:choose>
                        <c:when test="${adminCount >= maxAdmins}">
                            <span style="color: #ef4444;">At Capacity (10/10)</span>
                        </c:when>
                        <c:otherwise>
                            <span style="color: #10b981;"><c:out value="${maxAdmins - adminCount}"/> Slots Available</span>
                        </c:otherwise>
                    </c:choose>
                </div>
                <div class="stat-label">System Capacity Status</div>
            </div>
        </div>

        <!-- ===== ADD ADMIN FORM ===== -->
        <div class="glass-panel mb-3">
            <h3 class="section-title">
                <span class="title-accent">&#9830;</span> Add New Admin Account
            </h3>

            <c:choose>
                <c:when test="${adminCount >= maxAdmins}">
                    <div class="alert alert-error" style="margin-bottom: 0;">
                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
                        <strong>Maximum of 10 admins reached.</strong> Remove an existing admin before adding a new one.
                    </div>
                </c:when>
                <c:otherwise>
                    <form action="${pageContext.request.contextPath}/admin/admins" method="POST" id="add-admin-form" novalidate>
                        <input type="hidden" name="action" value="add">

                        <div class="card-grid" style="grid-template-columns: repeat(auto-fit, minmax(240px, 1fr)); gap: 1rem;">
                            <!-- Full Name -->
                            <div class="form-group">
                                <label for="admin-fullName" class="form-label">Full Name / Label <span class="required">*</span></label>
                                <input type="text" name="fullName" id="admin-fullName" class="form-control"
                                       placeholder="e.g. Coordinator Name" required maxlength="100">
                            </div>

                            <!-- Email -->
                            <div class="form-group">
                                <label for="admin-email" class="form-label">Admin Email Address <span class="required">*</span></label>
                                <input type="email" name="email" id="admin-email" class="form-control"
                                       placeholder="admin@college.edu" required maxlength="150">
                            </div>

                            <!-- Password -->
                            <div class="form-group">
                                <label for="admin-password" class="form-label">Password <span class="required">*</span></label>
                                <input type="password" name="password" id="admin-password" class="form-control"
                                       placeholder="Min 6 characters" required minlength="6" autocomplete="new-password">
                            </div>
                        </div>

                        <div style="margin-top: 1rem;">
                            <button type="submit" class="btn btn-primary" id="btn-add-admin">
                                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                    <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/>
                                    <circle cx="9" cy="7" r="4"/>
                                    <line x1="19" y1="8" x2="19" y2="14"/>
                                    <line x1="22" y1="11" x2="16" y2="11"/>
                                </svg>
                                Add Admin
                            </button>
                        </div>
                    </form>
                </c:otherwise>
            </c:choose>
        </div>

        <!-- ===== ADMIN LIST ===== -->
        <div class="glass-panel">
            <h3 class="section-title">
                <span class="title-accent">&#9830;</span> Current Administrators (${adminCount} / ${maxAdmins})
            </h3>

            <div class="table-wrapper">
                <table class="themed-table" id="admin-table">
                    <thead>
                        <tr>
                            <th>#</th>
                            <th>Name</th>
                            <th>Email</th>
                            <th>Date Added</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="adm" items="${admins}" varStatus="status">
                            <tr>
                                <td>${status.index + 1}</td>
                                <td>
                                    <strong><c:out value="${adm.fullName}"/></strong>
                                    <c:if test="${adm.adminId == sessionScope.adminId}">
                                        <span class="status-badge active" style="margin-left: 6px; font-size: 0.7rem; padding: 2px 6px;">You</span>
                                    </c:if>
                                </td>
                                <td><c:out value="${adm.email}"/></td>
                                <td>
                                    <fmt:formatDate value="${adm.createdAt}" pattern="yyyy-MM-dd HH:mm" />
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${adm.adminId == sessionScope.adminId}">
                                            <span style="color: var(--gray-400); font-size: 0.8rem; font-style: italic;">Current Session</span>
                                        </c:when>
                                        <c:when test="${adminCount <= 1}">
                                            <span style="color: var(--gray-400); font-size: 0.8rem; font-style: italic;">Sole Admin</span>
                                        </c:when>
                                        <c:otherwise>
                                            <form action="${pageContext.request.contextPath}/admin/admins" method="POST" style="display:inline;"
                                                  onsubmit="return confirm('Are you sure you want to remove admin account for \'${adm.fullName}\' (${adm.email})?');">
                                                <input type="hidden" name="action" value="delete">
                                                <input type="hidden" name="adminId" value="${adm.adminId}">
                                                <button type="submit" class="btn btn-danger btn-sm" title="Remove Admin">
                                                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                                        <polyline points="3 6 5 6 21 6"/>
                                                        <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
                                                        <line x1="10" y1="11" x2="10" y2="17"/>
                                                        <line x1="14" y1="11" x2="14" y2="17"/>
                                                    </svg>
                                                    Remove
                                                </button>
                                            </form>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>

    </div><!-- /page-container -->

    <!-- Footer -->
    <footer class="site-footer">
        <span class="footer-brand">PRAGMATRIX 2026</span> &mdash; Admin Management
    </footer>

    <script>
    /* Client validation for adding admin */
    var addAdminForm = document.getElementById('add-admin-form');
    if (addAdminForm) {
        addAdminForm.addEventListener('submit', function(e) {
            var name = document.getElementById('admin-fullName').value.trim();
            var email = document.getElementById('admin-email').value.trim();
            var pass = document.getElementById('admin-password').value;

            if (!name || !email || !pass) {
                e.preventDefault();
                alert('Please fill out all required fields.');
                return;
            }
            if (pass.length < 6) {
                e.preventDefault();
                alert('Password must be at least 6 characters.');
                return;
            }
        });
    }

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
