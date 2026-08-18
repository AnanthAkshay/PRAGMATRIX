package com.pragmatrix.servlet;

import com.pragmatrix.dao.RoundDAO;
import com.pragmatrix.dao.ScoreDAO;
import com.pragmatrix.dao.TeamDAO;
import com.pragmatrix.dao.VortexCriteriaDAO;
import com.pragmatrix.model.*;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class ExportScoresServletTest {

    @Test
    @DisplayName("formatMemberName handles all required cases (Rahul/Priya, NULL, empty, whitespace)")
    void testFormatMemberName() {
        // CASE 1: Value exists
        assertEquals("Rahul", ExportScoresServlet.formatMemberName("Rahul"));
        assertEquals("Priya", ExportScoresServlet.formatMemberName("Priya"));
        assertEquals("Priya", ExportScoresServlet.formatMemberName("  Priya  "));

        // CASE 2: NULL / Empty / Whitespace only -> NIL
        assertEquals("NIL", ExportScoresServlet.formatMemberName(null));
        assertEquals("NIL", ExportScoresServlet.formatMemberName(""));
        assertEquals("NIL", ExportScoresServlet.formatMemberName("   "));
        assertEquals("NIL", ExportScoresServlet.formatMemberName("\t\n"));
    }

    @Test
    @DisplayName("VORTEX summary export includes Member 2 and Member 3 in exact column order with NIL handling and scores unchanged")
    void testVortexWorkbookExport() throws SQLException, IOException {
        TeamDAO mockTeamDAO = new TeamDAO() {
            @Override
            public List<Team> findByQuizCode(String quizCode) {
                List<Team> list = new ArrayList<>();
                // Team 1: CASE 1 (Rahul | Priya)
                Team t1 = new Team("VORTEX", "MSRIT", "Akshay A", "ananth@msrit.edu", "Rahul", "Priya");
                t1.setUniqueId("PMVX001");
                list.add(t1);

                // Team 2: CASE 2 (NULL | NULL)
                Team t2 = new Team("VORTEX", "RNSIT", "Arjun B", "arjun@rnsit.edu", null, null);
                t2.setUniqueId("PMVX002");
                list.add(t2);

                // Team 3: CASE 3 (Rahul | NULL)
                Team t3 = new Team("VORTEX", "BMSCE", "Chetan C", "chetan@bmsce.edu", "Rahul", "   ");
                t3.setUniqueId("PMVX003");
                list.add(t3);

                // Team 4: CASE 4 (NULL | Priya)
                Team t4 = new Team("VORTEX", "RVCE", "Divya D", "divya@rvce.edu", "", "Priya");
                t4.setUniqueId("PMVX004");
                list.add(t4);

                return list;
            }
        };

        RoundDAO mockRoundDAO = new RoundDAO() {
            @Override
            public List<Round> findByQuizCode(String quizCode) {
                List<Round> list = new ArrayList<>();
                Round r1 = new Round(); r1.setRoundId(1); r1.setRoundNumber(1); r1.setRoundName("KAIROS");
                Round r2 = new Round(); r2.setRoundId(2); r2.setRoundNumber(2); r2.setRoundName("TREORAI");
                Round r3 = new Round(); r3.setRoundId(3); r3.setRoundNumber(3); r3.setRoundName("ENMA");
                Round r4 = new Round(); r4.setRoundId(4); r4.setRoundNumber(4); r4.setRoundName("GRAND FINALE");
                list.add(r1); list.add(r2); list.add(r3); list.add(r4);
                return list;
            }
        };

        ScoreDAO mockScoreDAO = new ScoreDAO() {
            @Override
            public Map<Integer, Score> findByTeam(String uniqueId) {
                Map<Integer, Score> map = new HashMap<>();
                if ("PMVX001".equals(uniqueId)) {
                    Score s1 = new Score(); s1.setPoints(85.0); map.put(1, s1);
                    Score s2 = new Score(); s2.setPoints(90.0); map.put(2, s2);
                    Score s3 = new Score(); s3.setPoints(88.0); map.put(3, s3);
                    Score s4 = new Score(); s4.setPoints(92.0); map.put(4, s4);
                } else if ("PMVX002".equals(uniqueId)) {
                    Score s1 = new Score(); s1.setPoints(78.0); map.put(1, s1);
                    Score s2 = new Score(); s2.setPoints(81.0); map.put(2, s2);
                    Score s3 = new Score(); s3.setPoints(75.0); map.put(3, s3);
                    Score s4 = new Score(); s4.setPoints(0.0);  map.put(4, s4);
                }
                return map;
            }
        };

        VortexCriteriaDAO mockVortexCriteriaDAO = new VortexCriteriaDAO() {
            @Override
            public List<VortexRound> getAllRounds() {
                List<VortexRound> vrList = new ArrayList<>();
                VortexRound vr1 = new VortexRound(1, "KAIROS", 1);
                JudgingComponent comp = new JudgingComponent(1, 1, "Analysis", 50, 1);
                JudgingCriterion crit1 = new JudgingCriterion(1, 1, "Clarity", "Judges clarity", 25, 1);
                JudgingCriterion crit2 = new JudgingCriterion(2, 1, "Depth", "Judges depth", 25, 2);
                comp.setCriteria(Arrays.asList(crit1, crit2));
                vr1.setComponents(Collections.singletonList(comp));
                vrList.add(vr1);
                return vrList;
            }

            @Override
            public Map<Integer, Double> getTeamScoresForRound(String uniqueId, int roundId) {
                Map<Integer, Double> scores = new HashMap<>();
                if ("PMVX001".equals(uniqueId)) {
                    scores.put(1, 22.5);
                    scores.put(2, 24.0);
                }
                return scores;
            }
        };

        ExportScoresServlet servlet = new ExportScoresServlet(mockTeamDAO, mockRoundDAO, mockScoreDAO, mockVortexCriteriaDAO);

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            servlet.buildVortexWorkbook(workbook);

            // 1. Vortex Summary Sheet
            Sheet summarySheet = workbook.getSheet("Vortex Summary");
            assertNotNull(summarySheet);

            // Row 3: Header
            Row headerRow = summarySheet.getRow(3);
            assertNotNull(headerRow);
            assertEquals("#", headerRow.getCell(0).getStringCellValue());
            assertEquals("Team Code", headerRow.getCell(1).getStringCellValue());
            assertEquals("College / Institution", headerRow.getCell(2).getStringCellValue());
            assertEquals("Team Lead Name", headerRow.getCell(3).getStringCellValue());
            assertEquals("Team Lead Email", headerRow.getCell(4).getStringCellValue());
            assertEquals("Member 2 Name", headerRow.getCell(5).getStringCellValue());
            assertEquals("Member 3 Name", headerRow.getCell(6).getStringCellValue());
            assertEquals("Round 1 (KAIROS)", headerRow.getCell(7).getStringCellValue());
            assertEquals("Round 2 (TREORAI)", headerRow.getCell(8).getStringCellValue());
            assertEquals("Round 3 (ENMA)", headerRow.getCell(9).getStringCellValue());
            assertEquals("Round 4 (GRAND FINALE)", headerRow.getCell(10).getStringCellValue());
            assertEquals("Total Score", headerRow.getCell(11).getStringCellValue());

            // CASE 1: Row 4 -> Team PMVX001 (Rahul | Priya)
            Row row1 = summarySheet.getRow(4);
            assertEquals(1, (int) row1.getCell(0).getNumericCellValue());
            assertEquals("PMVX001", row1.getCell(1).getStringCellValue());
            assertEquals("MSRIT", row1.getCell(2).getStringCellValue());
            assertEquals("Akshay A", row1.getCell(3).getStringCellValue());
            assertEquals("ananth@msrit.edu", row1.getCell(4).getStringCellValue());
            assertEquals("Rahul", row1.getCell(5).getStringCellValue());
            assertEquals("Priya", row1.getCell(6).getStringCellValue());
            assertEquals(85.0, row1.getCell(7).getNumericCellValue());
            assertEquals(90.0, row1.getCell(8).getNumericCellValue());
            assertEquals(88.0, row1.getCell(9).getNumericCellValue());
            assertEquals(92.0, row1.getCell(10).getNumericCellValue());
            assertEquals(355.0, row1.getCell(11).getNumericCellValue());

            // CASE 2: Row 5 -> Team PMVX002 (NIL | NIL)
            Row row2 = summarySheet.getRow(5);
            assertEquals(2, (int) row2.getCell(0).getNumericCellValue());
            assertEquals("PMVX002", row2.getCell(1).getStringCellValue());
            assertEquals("RNSIT", row2.getCell(2).getStringCellValue());
            assertEquals("Arjun B", row2.getCell(3).getStringCellValue());
            assertEquals("arjun@rnsit.edu", row2.getCell(4).getStringCellValue());
            assertEquals("NIL", row2.getCell(5).getStringCellValue());
            assertEquals("NIL", row2.getCell(6).getStringCellValue());
            assertEquals(78.0, row2.getCell(7).getNumericCellValue());
            assertEquals(81.0, row2.getCell(8).getNumericCellValue());
            assertEquals(75.0, row2.getCell(9).getNumericCellValue());
            assertEquals(0.0, row2.getCell(10).getNumericCellValue());
            assertEquals(234.0, row2.getCell(11).getNumericCellValue());

            // CASE 3: Row 6 -> Team PMVX003 (Rahul | NIL)
            Row row3 = summarySheet.getRow(6);
            assertEquals("Rahul", row3.getCell(5).getStringCellValue());
            assertEquals("NIL", row3.getCell(6).getStringCellValue());

            // CASE 4: Row 7 -> Team PMVX004 (NIL | Priya)
            Row row4 = summarySheet.getRow(7);
            assertEquals("NIL", row4.getCell(5).getStringCellValue());
            assertEquals("Priya", row4.getCell(6).getStringCellValue());

            // Exactly 4 teams -> row 8 is null
            assertNull(summarySheet.getRow(8));

            // 2. Vortex Detailed Breakdown Sheet
            Sheet r1Sheet = workbook.getSheet("R1 - KAIROS");
            assertNotNull(r1Sheet);

            Row r1Header = r1Sheet.getRow(2);
            assertNotNull(r1Header);
            assertEquals("#", r1Header.getCell(0).getStringCellValue());
            assertEquals("Team Code", r1Header.getCell(1).getStringCellValue());
            assertEquals("College", r1Header.getCell(2).getStringCellValue());
            assertEquals("Team Lead", r1Header.getCell(3).getStringCellValue());
            assertEquals("Member 2", r1Header.getCell(4).getStringCellValue());
            assertEquals("Member 3", r1Header.getCell(5).getStringCellValue());
            assertEquals("Clarity (25 pts)", r1Header.getCell(6).getStringCellValue());
            assertEquals("Depth (25 pts)", r1Header.getCell(7).getStringCellValue());
            assertEquals("Round Total", r1Header.getCell(8).getStringCellValue());

            Row r1Data1 = r1Sheet.getRow(3);
            assertEquals("PMVX001", r1Data1.getCell(1).getStringCellValue());
            assertEquals("Rahul", r1Data1.getCell(4).getStringCellValue());
            assertEquals("Priya", r1Data1.getCell(5).getStringCellValue());
            assertEquals(22.5, r1Data1.getCell(6).getNumericCellValue());
            assertEquals(24.0, r1Data1.getCell(7).getNumericCellValue());
            assertEquals(46.5, r1Data1.getCell(8).getNumericCellValue());

            Row r1Data2 = r1Sheet.getRow(4);
            assertEquals("PMVX002", r1Data2.getCell(1).getStringCellValue());
            assertEquals("NIL", r1Data2.getCell(4).getStringCellValue());
            assertEquals("NIL", r1Data2.getCell(5).getStringCellValue());
        }
    }

    @Test
    @DisplayName("BIZWIZX export preserves structure and includes Member 2 and Member 3 in exact column order")
    void testBizwizxWorkbookExport() throws SQLException, IOException {
        TeamDAO mockTeamDAO = new TeamDAO() {
            @Override
            public List<Team> findByQuizCode(String quizCode) {
                List<Team> list = new ArrayList<>();
                Team t1 = new Team("BIZWIZX", "MSRIT", "Akshay A", "akshay@msrit.edu", "MemberTwo", "MemberThree");
                t1.setUniqueId("PMBZ001");
                list.add(t1);

                Team t2 = new Team("BIZWIZX", "PES", "Kiran K", "kiran@pes.edu", null, null);
                t2.setUniqueId("PMBZ002");
                list.add(t2);
                return list;
            }
        };

        RoundDAO mockRoundDAO = new RoundDAO() {
            @Override
            public List<Round> findByQuizCode(String quizCode) {
                List<Round> list = new ArrayList<>();
                Round r1 = new Round(); r1.setRoundId(10); r1.setRoundNumber(1); r1.setRoundName("Aptitude");
                Round r2 = new Round(); r2.setRoundId(20); r2.setRoundNumber(2); r2.setRoundName("Finance");
                Round r3 = new Round(); r3.setRoundId(30); r3.setRoundNumber(3); r3.setRoundName("Marketing");
                Round r4 = new Round(); r4.setRoundId(40); r4.setRoundNumber(4); r4.setRoundName("HR");
                list.add(r1); list.add(r2); list.add(r3); list.add(r4);
                return list;
            }
        };

        ScoreDAO mockScoreDAO = new ScoreDAO() {
            @Override
            public Map<Integer, Score> findByTeam(String uniqueId) {
                Map<Integer, Score> map = new HashMap<>();
                if ("PMBZ001".equals(uniqueId)) {
                    Score s1 = new Score(); s1.setPoints(10.0); map.put(10, s1);
                    Score s2 = new Score(); s2.setPoints(20.0); map.put(20, s2);
                    Score s3 = new Score(); s3.setPoints(30.0); map.put(30, s3);
                    Score s4 = new Score(); s4.setPoints(40.0); map.put(40, s4);
                }
                return map;
            }
        };

        VortexCriteriaDAO mockVortexCriteriaDAO = new VortexCriteriaDAO();

        ExportScoresServlet servlet = new ExportScoresServlet(mockTeamDAO, mockRoundDAO, mockScoreDAO, mockVortexCriteriaDAO);

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            servlet.buildBizwizxWorkbook(workbook);

            Sheet sheet = workbook.getSheet("BizWizX Scores");
            assertNotNull(sheet);

            Row headerRow = sheet.getRow(3);
            assertNotNull(headerRow);
            assertEquals("#", headerRow.getCell(0).getStringCellValue());
            assertEquals("Team Code", headerRow.getCell(1).getStringCellValue());
            assertEquals("College / Institution", headerRow.getCell(2).getStringCellValue());
            assertEquals("Team Lead Name", headerRow.getCell(3).getStringCellValue());
            assertEquals("Team Lead Email", headerRow.getCell(4).getStringCellValue());
            assertEquals("Member 2 Name", headerRow.getCell(5).getStringCellValue());
            assertEquals("Member 3 Name", headerRow.getCell(6).getStringCellValue());
            assertEquals("Round 1 (Aptitude)", headerRow.getCell(7).getStringCellValue());
            assertEquals("Round 2 (Finance)", headerRow.getCell(8).getStringCellValue());
            assertEquals("Round 3 (Marketing)", headerRow.getCell(9).getStringCellValue());
            assertEquals("Round 4 (HR)", headerRow.getCell(10).getStringCellValue());
            assertEquals("Total Score", headerRow.getCell(11).getStringCellValue());

            Row row1 = sheet.getRow(4);
            assertEquals(1, (int) row1.getCell(0).getNumericCellValue());
            assertEquals("PMBZ001", row1.getCell(1).getStringCellValue());
            assertEquals("MSRIT", row1.getCell(2).getStringCellValue());
            assertEquals("Akshay A", row1.getCell(3).getStringCellValue());
            assertEquals("akshay@msrit.edu", row1.getCell(4).getStringCellValue());
            assertEquals("MemberTwo", row1.getCell(5).getStringCellValue());
            assertEquals("MemberThree", row1.getCell(6).getStringCellValue());
            assertEquals(10.0, row1.getCell(7).getNumericCellValue());
            assertEquals(20.0, row1.getCell(8).getNumericCellValue());
            assertEquals(30.0, row1.getCell(9).getNumericCellValue());
            assertEquals(40.0, row1.getCell(10).getNumericCellValue());
            assertEquals(100.0, row1.getCell(11).getNumericCellValue());

            Row row2 = sheet.getRow(5);
            assertEquals(2, (int) row2.getCell(0).getNumericCellValue());
            assertEquals("PMBZ002", row2.getCell(1).getStringCellValue());
            assertEquals("NIL", row2.getCell(5).getStringCellValue());
            assertEquals("NIL", row2.getCell(6).getStringCellValue());
        }
    }
}
