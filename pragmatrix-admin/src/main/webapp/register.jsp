<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="Register your team for PRAGMATRIX 2026 — BizWizX or Vortex inter-college quiz events.">
    <title>Register a Team — PRAGMATRIX 2026</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/theme.css">
</head>
<body>

    <!-- Header -->
    <header class="site-header">
        <nav class="header-nav">
            <a href="${pageContext.request.contextPath}/" class="header-brand">
                <img src="${pageContext.request.contextPath}/images/archway-bg.jpeg" alt="PRAGMATRIX" class="brand-logo">
                <span class="brand-text">Pragmatrix 2026</span>
            </a>
            <div class="header-links">
                <a href="${pageContext.request.contextPath}/">Home</a>
            </div>
        </nav>
    </header>

    <!-- Registration Form -->
    <div class="centered-form-wrapper">
        <div class="centered-form-card glass-panel">
            <div class="form-card-header">
                <img src="${pageContext.request.contextPath}/images/archway-bg.jpeg" alt="PRAGMATRIX 2026" class="form-logo">
                <h2>Register Your Team</h2>
                <p>Join the Applied Management Carnival</p>
            </div>

            <!-- Error alert -->
            <c:if test="${not empty error}">
                <div class="alert alert-error" id="error-alert">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
                    <c:out value="${error}"/>
                </div>
            </c:if>

            <form action="${pageContext.request.contextPath}/register" method="POST" id="register-form" novalidate>

                <!-- Quiz Selection -->
                <div class="form-group">
                    <label class="form-label">Select Event <span class="required">*</span></label>
                    <div class="radio-group">
                        <input type="radio" name="quizCode" id="quiz-bizwizx" value="BIZWIZX"
                               <c:if test="${quizCode == 'BIZWIZX' || empty quizCode}">checked</c:if>>
                        <label for="quiz-bizwizx">BizWizX</label>

                        <input type="radio" name="quizCode" id="quiz-vortex" value="VORTEX"
                               <c:if test="${quizCode == 'VORTEX'}">checked</c:if>>
                        <label for="quiz-vortex">Vortex</label>
                    </div>
                </div>

                <!-- College Name -->
                <div class="form-group">
                    <label for="collegeName" class="form-label">College Name <span class="required">*</span></label>
                    <input type="text" name="collegeName" id="collegeName" class="form-control"
                           placeholder="e.g. St. Xavier's College"
                           value="<c:out value='${collegeName}'/>"
                           required maxlength="150">
                </div>

                <!-- Team Lead Name -->
                <div class="form-group">
                    <label for="teamLeadName" class="form-label">Team Lead Name <span class="required">*</span></label>
                    <input type="text" name="teamLeadName" id="teamLeadName" class="form-control"
                           placeholder="Full name of the team lead"
                           value="<c:out value='${teamLeadName}'/>"
                           required maxlength="100">
                </div>

                <!-- Member 2 Name (Optional) -->
                <div class="form-group">
                    <label for="member2Name" class="form-label">Member 2 Name <span style="font-size: 0.85rem; font-weight: normal; color: var(--gray-400);">(Optional)</span></label>
                    <input type="text" name="member2Name" id="member2Name" class="form-control"
                           placeholder="Full name of member 2"
                           value="<c:out value='${member2Name}'/>"
                           maxlength="150">
                </div>

                <!-- Member 3 Name (Optional) -->
                <div class="form-group">
                    <label for="member3Name" class="form-label">Member 3 Name <span style="font-size: 0.85rem; font-weight: normal; color: var(--gray-400);">(Optional)</span></label>
                    <input type="text" name="member3Name" id="member3Name" class="form-control"
                           placeholder="Full name of member 3"
                           value="<c:out value='${member3Name}'/>"
                           maxlength="150">
                </div>

                <!-- Team Lead Email -->
                <div class="form-group">
                    <label for="leadEmail" class="form-label">Team Lead Email <span class="required">*</span></label>
                    <input type="email" name="leadEmail" id="leadEmail" class="form-control"
                           placeholder="teamlead@college.edu"
                           value="<c:out value='${leadEmail}'/>"
                           required maxlength="150">
                    <p class="form-hint">Participant ID will be emailed to this address</p>
                </div>

                <button type="submit" class="btn btn-primary btn-lg w-100" id="btn-submit-register">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/>
                        <circle cx="9" cy="7" r="4"/>
                        <line x1="19" y1="8" x2="19" y2="14"/>
                        <line x1="22" y1="11" x2="16" y2="11"/>
                    </svg>
                    Register Team
                </button>
            </form>

            <div class="form-footer">
                <a href="${pageContext.request.contextPath}/">&larr; Back to Home</a>
            </div>
        </div>
    </div>

    <!-- Client-side validation -->
    <script>
    document.getElementById('register-form').addEventListener('submit', function(e) {
        var college = document.getElementById('collegeName').value.trim();
        var leadName = document.getElementById('teamLeadName').value.trim();
        var leadEmail = document.getElementById('leadEmail').value.trim();
        var quizSelected = document.querySelector('input[name="quizCode"]:checked');
        var errors = [];

        if (!quizSelected) errors.push('Please select an event.');
        if (!college) errors.push('College name is required.');
        if (!leadName) errors.push('Team lead name is required.');
        if (!leadEmail) errors.push('Team lead email is required.');

        if (errors.length > 0) {
            e.preventDefault();
            var alertDiv = document.getElementById('error-alert');
            if (!alertDiv) {
                alertDiv = document.createElement('div');
                alertDiv.id = 'error-alert';
                alertDiv.className = 'alert alert-error';
                document.getElementById('register-form').parentNode.insertBefore(alertDiv, document.getElementById('register-form'));
            }
            alertDiv.innerHTML = '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg> ' + errors.join(' ');
        }
    });
    </script>

</body>
</html>
