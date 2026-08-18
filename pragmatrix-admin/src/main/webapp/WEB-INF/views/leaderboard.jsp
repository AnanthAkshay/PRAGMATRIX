<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Leaderboard — <c:out value="${selectedQuiz}"/> — PRAGMATRIX 2026</title>
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
                <a href="${pageContext.request.contextPath}/admin/dashboard?quiz=${selectedQuiz}">Dashboard</a>
                <a href="${pageContext.request.contextPath}/admin/leaderboard?quiz=${selectedQuiz}" class="active">Leaderboard</a>
                <a href="${pageContext.request.contextPath}/admin/admins">Admins</a>
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

        <h1 class="page-title">
            <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="var(--gold-600)" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="vertical-align: middle;">
                <path d="M6 9H4.5a2.5 2.5 0 0 1 0-5C7 4 6 9 6 9m12 0h1.5a2.5 2.5 0 0 0 0-5C17 4 18 9 18 9m-12 0a6 6 0 0 0 12 0M6 9h12m-6 9v3m-4 0h8"/>
            </svg>
            Leaderboard
        </h1>
        <p class="page-subtitle">Live rankings updated after each finished round</p>

        <!-- Quiz Tabs & Export Action -->
        <div class="d-flex justify-between align-center flex-wrap gap-md mb-2">
            <div class="quiz-tabs" style="margin-bottom: 0;">
                <a href="${pageContext.request.contextPath}/admin/leaderboard?quiz=BIZWIZX"
                   class="quiz-tab <c:if test='${selectedQuiz == "BIZWIZX"}'>active</c:if>"
                   id="lb-tab-bizwizx">BizWizX</a>
                <a href="${pageContext.request.contextPath}/admin/leaderboard?quiz=VORTEX"
                   class="quiz-tab <c:if test='${selectedQuiz == "VORTEX"}'>active</c:if>"
                   id="lb-tab-vortex">Vortex</a>
            </div>
            <div>
                <a href="${pageContext.request.contextPath}/admin/export-scores?quiz=${selectedQuiz}"
                   class="btn btn-primary btn-sm" id="btn-lb-export-scores" style="background: linear-gradient(135deg, #107c41, #1e8e3e); border-color: #107c41; display: inline-flex; align-items: center; gap: 0.4rem;"
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

        <!-- Auto-refresh indicator -->
        <div class="d-flex justify-between align-center flex-wrap gap-sm mb-2">
            <span class="text-sm text-muted" id="refresh-status">
                <span class="spinner" style="width:14px; height:14px; border-width:2px; vertical-align:middle; margin-right: 0.3rem;"></span>
                Auto-refreshing every 10 seconds
            </span>
            <span class="text-sm text-muted" id="last-updated"></span>
        </div>

        <!-- Leaderboard Table -->
        <div class="glass-panel">
            <div class="table-wrapper">
                <table class="themed-table" id="leaderboard-table">
                    <thead>
                        <tr>
                            <th>Rank</th>
                            <th>Team ID</th>
                            <th>College</th>
                            <th>Team Lead</th>
                            <c:forEach var="round" items="${rounds}">
                                <th>
                                    <c:out value="${round.roundName}"/>
                                    <c:if test="${!round.finished}">
                                        <br><span style="font-size: 0.65rem; font-family: var(--font-body); opacity: 0.7; font-weight: 400;">pending</span>
                                    </c:if>
                                </th>
                            </c:forEach>
                            <th style="background: var(--gold-700); color: var(--white);">Total</th>
                        </tr>
                    </thead>
                    <tbody id="leaderboard-tbody">
                        <c:forEach var="entry" items="${entries}" varStatus="status">
                            <c:set var="rank" value="${entry.rank > 0 ? entry.rank : (status.index + 1)}"/>
                            <tr class="<c:choose><c:when test='${rank == 1}'>rank-1</c:when><c:when test='${rank == 2}'>rank-2</c:when><c:when test='${rank == 3}'>rank-3</c:when></c:choose>">
                                <td>
                                    <c:choose>
                                        <c:when test="${rank == 1}"><span class="rank-badge gold">1</span></c:when>
                                        <c:when test="${rank == 2}"><span class="rank-badge silver">2</span></c:when>
                                        <c:when test="${rank == 3}"><span class="rank-badge bronze">3</span></c:when>
                                        <c:otherwise><span class="rank-badge default">${rank}</span></c:otherwise>
                                    </c:choose>
                                    <c:if test="${entry.tied}">
                                        <span class="badge" style="background: #f59e0b; color: #000000; font-size: 0.65rem; font-weight: 700; padding: 0.1rem 0.35rem; border-radius: 3px; margin-left: 0.25rem; vertical-align: middle;">TIED</span>
                                    </c:if>
                                </td>
                                <td><strong style="color: var(--purple-700);"><c:out value="${entry.uniqueId}"/></strong></td>
                                <td><c:out value="${entry.collegeName}"/></td>
                                <td>
                                    <c:out value="${entry.teamLeadName}"/>
                                </td>
                                <c:forEach var="round" items="${rounds}">
                                    <td style="text-align: center;">
                                        <c:choose>
                                            <c:when test="${round.finished && entry.roundPoints[round.roundNumber] != null}">
                                                <fmt:formatNumber value="${entry.roundPoints[round.roundNumber]}" maxFractionDigits="2"/>
                                            </c:when>
                                            <c:otherwise>
                                                <span style="color: var(--gray-400);">&mdash;</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                </c:forEach>
                                <td style="text-align: center; background: rgba(212, 175, 55, 0.08);">
                                    <strong style="color: var(--gold-700); font-size: 1.05rem;">
                                        <fmt:formatNumber value="${entry.totalPoints}" maxFractionDigits="2"/>
                                    </strong>
                                </td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty entries}">
                            <tr>
                                <td colspan="${4 + rounds.size() + 1}" style="text-align: center; padding: 2rem; color: var(--gray-500);">
                                    No teams registered yet.
                                </td>
                            </tr>
                        </c:if>
                    </tbody>
                </table>
            </div>
        </div>

    </div><!-- /page-container -->

    <footer class="site-footer">
        <span class="footer-brand">PRAGMATRIX 2026</span> &mdash; Leaderboard
    </footer>

    <script>
    /* Auto-refresh leaderboard via AJAX every 10 seconds */
    (function() {
        var quizCode = '<c:out value="${selectedQuiz}"/>';
        var baseUrl = '${pageContext.request.contextPath}/admin/leaderboard';

        function updateTimestamp() {
            var now = new Date();
            var el = document.getElementById('last-updated');
            if (el) el.textContent = 'Last updated: ' + now.toLocaleTimeString();
        }

        function escapeHtml(str) {
            if (!str) return '';
            var div = document.createElement('div');
            div.appendChild(document.createTextNode(str));
            return div.innerHTML;
        }

        function formatScore(val) {
            if (val === null || val === undefined || isNaN(val)) return '0';
            var num = Number(val);
            return Number(num.toFixed(2)).toString();
        }

        function refreshLeaderboard() {
            fetch(baseUrl + '?quiz=' + quizCode + '&format=json')
                .then(function(res) {
                    if (!res.ok) throw new Error('HTTP ' + res.status);
                    return res.json();
                })
                .then(function(data) {
                    var tbody = document.getElementById('leaderboard-tbody');
                    if (!tbody || !data) return;

                    if (!data.entries || data.entries.length === 0) {
                        var colCount = 4 + (data.rounds ? data.rounds.length : 0) + 1;
                        tbody.innerHTML = '<tr><td colspan="' + colCount + '" style="text-align: center; padding: 2rem; color: var(--gray-500);">No teams registered yet.</td></tr>';
                        updateTimestamp();
                        return;
                    }

                    var html = '';
                    data.entries.forEach(function(entry, index) {
                        var rank = entry.rank || (index + 1);
                        var rankClass = rank <= 3 ? 'rank-' + rank : '';
                        var rankBadge = '';
                        if (rank === 1) rankBadge = '<span class="rank-badge gold">1</span>';
                        else if (rank === 2) rankBadge = '<span class="rank-badge silver">2</span>';
                        else if (rank === 3) rankBadge = '<span class="rank-badge bronze">3</span>';
                        else rankBadge = '<span class="rank-badge default">' + rank + '</span>';

                        if (entry.tied) {
                            rankBadge += ' <span class="badge" style="background: #f59e0b; color: #000000; font-size: 0.65rem; font-weight: 700; padding: 0.1rem 0.35rem; border-radius: 3px; margin-left: 0.25rem; vertical-align: middle;">TIED</span>';
                        }

                        var uniqueId = escapeHtml(entry.uniqueId || '');
                        var collegeName = escapeHtml(entry.collegeName || '');
                        var leadName = escapeHtml(entry.teamLeadName || '');

                        html += '<tr class="' + rankClass + '">';
                        html += '<td>' + rankBadge + '</td>';
                        html += '<td><strong style="color:var(--purple-700);">' + uniqueId + '</strong></td>';
                        html += '<td>' + collegeName + '</td>';
                        html += '<td>' + leadName + '</td>';

                        if (data.rounds) {
                            data.rounds.forEach(function(round) {
                                var isFinished = (round.isFinished === true || round.finished === true);
                                var roundNum = round.roundNumber;
                                var pts = null;
                                if (entry.roundPoints) {
                                    if (entry.roundPoints[roundNum] !== undefined && entry.roundPoints[roundNum] !== null) {
                                        pts = entry.roundPoints[roundNum];
                                    } else if (entry.roundPoints[String(roundNum)] !== undefined && entry.roundPoints[String(roundNum)] !== null) {
                                        pts = entry.roundPoints[String(roundNum)];
                                    }
                                }

                                html += '<td style="text-align:center;">';
                                if (isFinished && pts !== null && pts !== undefined) {
                                    html += formatScore(pts);
                                } else {
                                    html += '<span style="color:var(--gray-400);">&mdash;</span>';
                                }
                                html += '</td>';
                            });
                        }

                        var total = entry.totalPoints != null ? entry.totalPoints : 0;
                        html += '<td style="text-align:center;background:rgba(212,175,55,0.08);">';
                        html += '<strong style="color:var(--gold-700);font-size:1.05rem;">' + formatScore(total) + '</strong>';
                        html += '</td></tr>';
                    });

                    tbody.innerHTML = html;
                    updateTimestamp();
                })
                .catch(function(err) {
                    console.error('Leaderboard refresh error:', err);
                });
        }

        updateTimestamp();
        setInterval(refreshLeaderboard, 10000);
    })();
    </script>

</body>
</html>
