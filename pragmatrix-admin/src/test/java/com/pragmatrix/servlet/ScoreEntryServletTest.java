package com.pragmatrix.servlet;

import com.pragmatrix.dao.RoundDAO;
import com.pragmatrix.dao.ScoreDAO;
import com.pragmatrix.dao.TeamDAO;
import com.pragmatrix.dao.VortexCriteriaDAO;
import com.pragmatrix.model.JudgingComponent;
import com.pragmatrix.model.JudgingCriterion;
import com.pragmatrix.model.Round;
import com.pragmatrix.model.Team;
import com.pragmatrix.model.VortexRound;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class ScoreEntryServletTest {

    private HttpSession createMockSession() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("adminId", 1);
        attributes.put("adminName", "Test Admin");
        return (HttpSession) Proxy.newProxyInstance(
                HttpSession.class.getClassLoader(),
                new Class<?>[]{HttpSession.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("getAttribute".equals(name)) {
                        return attributes.get(args[0]);
                    } else if ("setAttribute".equals(name)) {
                        attributes.put((String) args[0], args[1]);
                        return null;
                    }
                    return null;
                }
        );
    }

    private void injectField(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }

    @Test
    @DisplayName("ScoreEntryServlet returns JSON success on AJAX save for single team in VORTEX")
    void testAjaxSaveTeamScoresSuccess() throws Exception {
        ScoreEntryServlet servlet = new ScoreEntryServlet();

        Round mockRound = new Round();
        mockRound.setRoundId(1);
        mockRound.setRoundName("KAIROS");
        mockRound.setRoundNumber(1);
        mockRound.setQuizCode("VORTEX");
        mockRound.setFinished(false);

        RoundDAO mockRoundDAO = new RoundDAO() {
            @Override
            public Round findById(int id) {
                return mockRound;
            }
        };

        Team team1 = new Team();
        team1.setUniqueId("PMVX001");
        team1.setCollegeName("Test College");
        team1.setTeamLeadName("Lead 1");

        Team team2 = new Team();
        team2.setUniqueId("PMVX002");
        team2.setCollegeName("Other College");
        team2.setTeamLeadName("Lead 2");

        TeamDAO mockTeamDAO = new TeamDAO() {
            @Override
            public List<Team> findByQuizCode(String quizCode) {
                return Arrays.asList(team1, team2);
            }
        };

        VortexRound vRound = new VortexRound();
        vRound.setRoundId(1);
        vRound.setRoundName("KAIROS");
        vRound.setDisplayOrder(1);

        JudgingComponent comp = new JudgingComponent();
        comp.setComponentId(1);
        comp.setComponentLabel("Presentation");

        JudgingCriterion crit = new JudgingCriterion();
        crit.setCriterionId(101);
        crit.setCriterionName("Clarity");
        crit.setMaxMarks(20);
        comp.setCriteria(Collections.singletonList(crit));
        vRound.setComponents(Collections.singletonList(comp));

        AtomicReference<String> savedTeamRef = new AtomicReference<>();
        AtomicReference<Map<Integer, Double>> savedScoresRef = new AtomicReference<>();

        VortexCriteriaDAO mockVortexDAO = new VortexCriteriaDAO() {
            @Override
            public VortexRound getRoundByName(String name) {
                return vRound;
            }

            @Override
            public boolean saveTeamScores(String teamId, int vortexRoundId, int roundId, Map<Integer, Double> criterionScores, String scoredBy) {
                savedTeamRef.set(teamId);
                savedScoresRef.set(criterionScores);
                return true;
            }
        };

        injectField(servlet, "roundDAO", mockRoundDAO);
        injectField(servlet, "teamDAO", mockTeamDAO);
        injectField(servlet, "vortexDAO", mockVortexDAO);

        HttpSession session = createMockSession();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        Map<String, String> params = new HashMap<>();
        params.put("roundId", "1");
        params.put("targetTeam", "PMVX001");
        params.put("score_PMVX001_101", "18.5");
        params.put("score_PMVX002_101", "15.0"); // Team 2 scores shouldn't be processed when targetTeam=PMVX001

        HttpServletRequest req = (HttpServletRequest) Proxy.newProxyInstance(
                HttpServletRequest.class.getClassLoader(),
                new Class<?>[]{HttpServletRequest.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("getSession".equals(name)) return session;
                    if ("getHeader".equals(name)) {
                        if ("X-Requested-With".equals(args[0])) return "XMLHttpRequest";
                        return null;
                    }
                    if ("getParameter".equals(name)) {
                        return params.get(args[0]);
                    }
                    if ("setCharacterEncoding".equals(name)) return null;
                    return null;
                }
        );

        HttpServletResponse resp = (HttpServletResponse) Proxy.newProxyInstance(
                HttpServletResponse.class.getClassLoader(),
                new Class<?>[]{HttpServletResponse.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("getWriter".equals(name)) return pw;
                    if ("setContentType".equals(name) || "setCharacterEncoding".equals(name)) return null;
                    return null;
                }
        );

        servlet.doPost(req, resp);
        pw.flush();

        String jsonOutput = sw.toString();
        assertTrue(jsonOutput.contains("\"success\":true"), "Response must indicate success: " + jsonOutput);
        assertTrue(jsonOutput.contains("\"targetTeam\":\"PMVX001\""), "Response must contain targetTeam: " + jsonOutput);
        assertEquals("PMVX001", savedTeamRef.get(), "Only targeted team scores should be saved");
        assertNotNull(savedScoresRef.get());
        assertEquals(18.5, savedScoresRef.get().get(101));
    }

    @Test
    @DisplayName("ScoreEntryServlet returns JSON error when score exceeds max marks in AJAX call")
    void testAjaxSaveExceedsMaxMarks() throws Exception {
        ScoreEntryServlet servlet = new ScoreEntryServlet();

        Round mockRound = new Round();
        mockRound.setRoundId(1);
        mockRound.setRoundName("KAIROS");
        mockRound.setRoundNumber(1);
        mockRound.setQuizCode("VORTEX");
        mockRound.setFinished(false);

        RoundDAO mockRoundDAO = new RoundDAO() {
            @Override
            public Round findById(int id) {
                return mockRound;
            }
        };

        Team team1 = new Team();
        team1.setUniqueId("PMVX001");
        team1.setCollegeName("Test College");

        TeamDAO mockTeamDAO = new TeamDAO() {
            @Override
            public List<Team> findByQuizCode(String quizCode) {
                return Collections.singletonList(team1);
            }
        };

        VortexRound vRound = new VortexRound();
        vRound.setRoundId(1);
        vRound.setRoundName("KAIROS");

        JudgingComponent comp = new JudgingComponent();
        comp.setComponentId(1);
        JudgingCriterion crit = new JudgingCriterion();
        crit.setCriterionId(101);
        crit.setMaxMarks(20);
        comp.setCriteria(Collections.singletonList(crit));
        vRound.setComponents(Collections.singletonList(comp));

        VortexCriteriaDAO mockVortexDAO = new VortexCriteriaDAO() {
            @Override
            public VortexRound getRoundByName(String name) {
                return vRound;
            }
        };

        injectField(servlet, "roundDAO", mockRoundDAO);
        injectField(servlet, "teamDAO", mockTeamDAO);
        injectField(servlet, "vortexDAO", mockVortexDAO);

        HttpSession session = createMockSession();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        Map<String, String> params = new HashMap<>();
        params.put("roundId", "1");
        params.put("targetTeam", "PMVX001");
        params.put("score_PMVX001_101", "25.0"); // 25 > max 20

        HttpServletRequest req = (HttpServletRequest) Proxy.newProxyInstance(
                HttpServletRequest.class.getClassLoader(),
                new Class<?>[]{HttpServletRequest.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("getSession".equals(name)) return session;
                    if ("getHeader".equals(name)) {
                        if ("X-Requested-With".equals(args[0])) return "XMLHttpRequest";
                        return null;
                    }
                    if ("getParameter".equals(name)) return params.get(args[0]);
                    if ("setCharacterEncoding".equals(name)) return null;
                    return null;
                }
        );

        HttpServletResponse resp = (HttpServletResponse) Proxy.newProxyInstance(
                HttpServletResponse.class.getClassLoader(),
                new Class<?>[]{HttpServletResponse.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("getWriter".equals(name)) return pw;
                    if ("setContentType".equals(name) || "setCharacterEncoding".equals(name)) return null;
                    return null;
                }
        );

        servlet.doPost(req, resp);
        pw.flush();

        String jsonOutput = sw.toString();
        assertTrue(jsonOutput.contains("\"success\":false"), "Response must indicate failure: " + jsonOutput);
        assertTrue(jsonOutput.contains("exceeds max marks of 20"), "Error message must mention exceeding max marks: " + jsonOutput);
    }
}
