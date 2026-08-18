package com.pragmatrix.servlet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.pragmatrix.dao.RoundDAO;
import com.pragmatrix.dao.ScoreDAO;
import com.pragmatrix.model.LeaderboardEntry;
import com.pragmatrix.model.Round;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class LeaderboardServletTest {

    private void injectField(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }

    @Test
    @DisplayName("LeaderboardServlet returns complete JSON payload with entries, roundPoints, and isFinished on AJAX polling")
    void testJsonLeaderboardResponse() throws Exception {
        LeaderboardServlet servlet = new LeaderboardServlet();

        Round r1 = new Round();
        r1.setRoundId(1);
        r1.setRoundNumber(1);
        r1.setRoundName("Round 1");
        r1.setQuizCode("BIZWIZX");
        r1.setFinished(true);

        Round r2 = new Round();
        r2.setRoundId(2);
        r2.setRoundNumber(2);
        r2.setRoundName("Round 2");
        r2.setQuizCode("BIZWIZX");
        r2.setFinished(true);

        Round r3 = new Round();
        r3.setRoundId(3);
        r3.setRoundNumber(3);
        r3.setRoundName("Round 3");
        r3.setQuizCode("BIZWIZX");
        r3.setFinished(true);

        Round r4 = new Round();
        r4.setRoundId(4);
        r4.setRoundNumber(4);
        r4.setRoundName("Round 4");
        r4.setQuizCode("BIZWIZX");
        r4.setFinished(false);

        List<Round> mockRounds = Arrays.asList(r1, r2, r3, r4);

        LeaderboardEntry e1 = new LeaderboardEntry();
        e1.setUniqueId("PMBZ004");
        e1.setCollegeName("St. Joseph College");
        e1.setTeamLeadName("Akshay");
        e1.setQuizCode("BIZWIZX");
        e1.setRank(1);
        e1.setTotalPoints(322.0);
        e1.putRoundPoints(1, 88.0);
        e1.putRoundPoints(2, 78.0);
        e1.putRoundPoints(3, 56.0);
        e1.putRoundPoints(4, 100.0);

        LeaderboardEntry e2 = new LeaderboardEntry();
        e2.setUniqueId("PMBZ002");
        e2.setCollegeName("Christ College");
        e2.setTeamLeadName("Varun");
        e2.setQuizCode("BIZWIZX");
        e2.setRank(2);
        e2.setTotalPoints(300.0);
        e2.putRoundPoints(1, 99.0);
        e2.putRoundPoints(2, 67.0);
        e2.putRoundPoints(3, 100.0);
        e2.putRoundPoints(4, 34.0);

        List<LeaderboardEntry> mockEntries = Arrays.asList(e1, e2);

        RoundDAO mockRoundDAO = new RoundDAO() {
            @Override
            public List<Round> findByQuizCode(String quizCode) {
                return mockRounds;
            }
        };

        ScoreDAO mockScoreDAO = new ScoreDAO() {
            @Override
            public List<LeaderboardEntry> getLeaderboard(String quizCode) {
                return mockEntries;
            }
        };

        injectField(servlet, "roundDAO", mockRoundDAO);
        injectField(servlet, "scoreDAO", mockScoreDAO);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        Map<String, String> params = new HashMap<>();
        params.put("quiz", "BIZWIZX");
        params.put("format", "json");

        HttpServletRequest req = (HttpServletRequest) Proxy.newProxyInstance(
                HttpServletRequest.class.getClassLoader(),
                new Class<?>[]{HttpServletRequest.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("getParameter".equals(name)) return params.get(args[0]);
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

        servlet.doGet(req, resp);
        pw.flush();

        String json = sw.toString();
        assertNotNull(json);
        assertTrue(json.contains("\"entries\""), "JSON must contain entries: " + json);
        assertTrue(json.contains("\"rounds\""), "JSON must contain rounds: " + json);
        assertTrue(json.contains("\"PMBZ004\""), "JSON must contain PMBZ004: " + json);
        assertTrue(json.contains("\"roundPoints\""), "JSON must contain roundPoints: " + json);
        assertTrue(json.contains("\"isFinished\""), "JSON must contain isFinished: " + json);

        JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
        assertEquals(2, obj.getAsJsonArray("entries").size());
        assertEquals(4, obj.getAsJsonArray("rounds").size());

        // Verify round 1 isFinished is true
        JsonObject jsonR1 = obj.getAsJsonArray("rounds").get(0).getAsJsonObject();
        assertTrue(jsonR1.get("isFinished").getAsBoolean());

        // Verify round 4 isFinished is false
        JsonObject jsonR4 = obj.getAsJsonArray("rounds").get(3).getAsJsonObject();
        assertFalse(jsonR4.get("isFinished").getAsBoolean());

        // Verify roundPoints map in PMBZ004 contains round 1 = 88.0
        JsonObject jsonE1 = obj.getAsJsonArray("entries").get(0).getAsJsonObject();
        JsonObject rp1 = jsonE1.getAsJsonObject("roundPoints");
        assertEquals(88.0, rp1.get("1").getAsDouble());
        assertEquals(78.0, rp1.get("2").getAsDouble());
        assertEquals(56.0, rp1.get("3").getAsDouble());
        assertEquals(100.0, rp1.get("4").getAsDouble());
        assertEquals(322.0, jsonE1.get("totalPoints").getAsDouble());
    }

    @Test
    @DisplayName("LeaderboardServlet forwards to JSP on regular HTML request")
    void testHtmlLeaderboardResponse() throws Exception {
        LeaderboardServlet servlet = new LeaderboardServlet();

        RoundDAO mockRoundDAO = new RoundDAO() {
            @Override
            public List<Round> findByQuizCode(String quizCode) {
                return Collections.emptyList();
            }
        };

        ScoreDAO mockScoreDAO = new ScoreDAO() {
            @Override
            public List<LeaderboardEntry> getLeaderboard(String quizCode) {
                return Collections.emptyList();
            }
        };

        injectField(servlet, "roundDAO", mockRoundDAO);
        injectField(servlet, "scoreDAO", mockScoreDAO);

        Map<String, String> params = new HashMap<>();
        params.put("quiz", "BIZWIZX");

        Map<String, Object> reqAttributes = new HashMap<>();
        AtomicReference<String> forwardedPath = new AtomicReference<>();

        RequestDispatcher dispatcher = (RequestDispatcher) Proxy.newProxyInstance(
                RequestDispatcher.class.getClassLoader(),
                new Class<?>[]{RequestDispatcher.class},
                (proxy, method, args) -> {
                    if ("forward".equals(method.getName())) {
                        return null;
                    }
                    return null;
                }
        );

        HttpServletRequest req = (HttpServletRequest) Proxy.newProxyInstance(
                HttpServletRequest.class.getClassLoader(),
                new Class<?>[]{HttpServletRequest.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("getParameter".equals(name)) return params.get(args[0]);
                    if ("setAttribute".equals(name)) {
                        reqAttributes.put((String) args[0], args[1]);
                        return null;
                    }
                    if ("getAttribute".equals(name)) return reqAttributes.get(args[0]);
                    if ("getRequestDispatcher".equals(name)) {
                        forwardedPath.set((String) args[0]);
                        return dispatcher;
                    }
                    return null;
                }
        );

        HttpServletResponse resp = (HttpServletResponse) Proxy.newProxyInstance(
                HttpServletResponse.class.getClassLoader(),
                new Class<?>[]{HttpServletResponse.class},
                (proxy, method, args) -> null
        );

        servlet.doGet(req, resp);

        assertEquals("/WEB-INF/views/leaderboard.jsp", forwardedPath.get());
        assertTrue(reqAttributes.containsKey("entries"));
        assertTrue(reqAttributes.containsKey("rounds"));
        assertEquals("BIZWIZX", reqAttributes.get("selectedQuiz"));
    }
}
