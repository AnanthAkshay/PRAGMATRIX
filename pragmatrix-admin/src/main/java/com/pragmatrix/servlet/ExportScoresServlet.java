package com.pragmatrix.servlet;

import com.pragmatrix.dao.RoundDAO;
import com.pragmatrix.dao.ScoreDAO;
import com.pragmatrix.dao.TeamDAO;
import com.pragmatrix.dao.VortexCriteriaDAO;
import com.pragmatrix.model.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Exports tournament scores to a styled Excel (.xlsx) workbook using Apache POI.
 * Accessible to authenticated administrators.
 *
 * GET /admin/export-scores?quiz=BIZWIZX
 * GET /admin/export-scores?quiz=VORTEX
 */
@WebServlet(name = "ExportScoresServlet", urlPatterns = {"/admin/export-scores"})
public class ExportScoresServlet extends HttpServlet {

    private final TeamDAO teamDAO = new TeamDAO();
    private final RoundDAO roundDAO = new RoundDAO();
    private final ScoreDAO scoreDAO = new ScoreDAO();
    private final VortexCriteriaDAO vortexCriteriaDAO = new VortexCriteriaDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("adminId") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String quizCode = req.getParameter("quiz");
        if (quizCode == null || quizCode.trim().isEmpty()) {
            quizCode = "BIZWIZX";
        }
        quizCode = quizCode.trim().toUpperCase();

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {

            if ("VORTEX".equals(quizCode)) {
                buildVortexWorkbook(workbook);
            } else {
                buildBizwizxWorkbook(workbook);
            }

            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String fileName = "PRAGMATRIX_2026_" + quizCode + "_Scores_" + timestamp + ".xlsx";

            resp.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            resp.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
            resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            resp.setHeader("Pragma", "no-cache");
            resp.setDateHeader("Expires", 0);

            try (OutputStream out = resp.getOutputStream()) {
                workbook.write(out);
                out.flush();
            }

        } catch (Exception e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error generating Excel scoresheet: " + e.getMessage());
        }
    }

    /**
     * Builds the Excel workbook for BIZWIZX.
     */
    private void buildBizwizxWorkbook(XSSFWorkbook workbook) throws SQLException {
        Sheet sheet = workbook.createSheet("BizWizX Scores");
        sheet.setDisplayGridlines(true);

        List<Team> teams = teamDAO.findByQuizCode("BIZWIZX");
        List<Round> rounds = roundDAO.findByQuizCode("BIZWIZX");

        // Map team uniqueId -> (roundId -> Score)
        Map<String, Map<Integer, Score>> teamScoreMap = new HashMap<>();
        for (Team t : teams) {
            teamScoreMap.put(t.getUniqueId(), scoreDAO.findByTeam(t.getUniqueId()));
        }

        // Styles
        CellStyle titleStyle = createTitleStyle(workbook, "BizWizX");
        CellStyle subtitleStyle = createSubtitleStyle(workbook);
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook, false);
        CellStyle dataStyleAlt = createDataStyle(workbook, true);
        CellStyle centerStyle = createCenterStyle(workbook, false);
        CellStyle centerStyleAlt = createCenterStyle(workbook, true);
        CellStyle scoreStyle = createScoreStyle(workbook, false);
        CellStyle scoreStyleAlt = createScoreStyle(workbook, true);
        CellStyle totalStyle = createTotalStyle(workbook);

        int totalColumns = 5 + rounds.size() + 1; // #, Code, College, Lead, Email, [Rounds...], Total

        // Title row
        Row titleRow = sheet.createRow(0);
        titleRow.setHeightInPoints(28);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("PRAGMATRIX 2026 — BIZWIZX SCORESHEET");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, totalColumns - 1));

        // Subtitle row
        Row subRow = sheet.createRow(1);
        subRow.setHeightInPoints(18);
        Cell subCell = subRow.createCell(0);
        String genTime = new SimpleDateFormat("dd MMMM yyyy, hh:mm a").format(new Date());
        subCell.setCellValue("Generated on: " + genTime + " | Post Graduate Department of Business Administration, Seshadripuram College");
        subCell.setCellStyle(subtitleStyle);
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, totalColumns - 1));

        // Blank separator row
        sheet.createRow(2).setHeightInPoints(10);

        // Header row
        Row headerRow = sheet.createRow(3);
        headerRow.setHeightInPoints(24);

        int colIdx = 0;
        createCell(headerRow, colIdx++, "#", headerStyle);
        createCell(headerRow, colIdx++, "Team Code", headerStyle);
        createCell(headerRow, colIdx++, "College / Institution", headerStyle);
        createCell(headerRow, colIdx++, "Team Lead Name", headerStyle);
        createCell(headerRow, colIdx++, "Team Lead Email", headerStyle);

        for (Round r : rounds) {
            String roundLabel = "Round " + r.getRoundNumber() + (r.getRoundName() != null && !r.getRoundName().isEmpty() ? " (" + r.getRoundName() + ")" : "");
            createCell(headerRow, colIdx++, roundLabel, headerStyle);
        }
        createCell(headerRow, colIdx++, "Total Score", headerStyle);

        // Data rows
        int rowIdx = 4;
        for (int i = 0; i < teams.size(); i++) {
            Team team = teams.get(i);
            boolean isAlt = (i % 2 == 1);
            CellStyle curData = isAlt ? dataStyleAlt : dataStyle;
            CellStyle curCenter = isAlt ? centerStyleAlt : centerStyle;
            CellStyle curScore = isAlt ? scoreStyleAlt : scoreStyle;

            Row row = sheet.createRow(rowIdx++);
            row.setHeightInPoints(20);

            colIdx = 0;
            createCell(row, colIdx++, i + 1, curCenter);
            createCell(row, colIdx++, team.getUniqueId(), curCenter);
            createCell(row, colIdx++, team.getCollegeName(), curData);
            createCell(row, colIdx++, team.getTeamLeadName(), curData);
            createCell(row, colIdx++, team.getLeadEmail(), curData);

            Map<Integer, Score> scores = teamScoreMap.getOrDefault(team.getUniqueId(), Collections.emptyMap());
            double total = 0;

            for (Round r : rounds) {
                Score s = scores.get(r.getRoundId());
                double pts = (s != null) ? s.getPoints() : 0.0;
                total += pts;
                createCell(row, colIdx++, pts, curScore);
            }

            createCell(row, colIdx++, total, totalStyle);
        }

        // Auto-fit columns
        for (int c = 0; c < totalColumns; c++) {
            sheet.autoSizeColumn(c);
            int width = sheet.getColumnWidth(c);
            sheet.setColumnWidth(c, Math.max(width + 1200, 3200));
        }
    }

    /**
     * Builds the Excel workbook for VORTEX with master summary + individual round breakdown sheets.
     */
    private void buildVortexWorkbook(XSSFWorkbook workbook) throws SQLException {
        List<Team> teams = teamDAO.findByQuizCode("VORTEX");
        List<Round> masterRounds = roundDAO.findByQuizCode("VORTEX");
        List<VortexRound> vortexRounds = vortexCriteriaDAO.getAllRounds();

        // 1. MASTER SUMMARY SHEET
        Sheet summarySheet = workbook.createSheet("Vortex Summary");
        summarySheet.setDisplayGridlines(true);

        CellStyle titleStyle = createTitleStyle(workbook, "Vortex");
        CellStyle subtitleStyle = createSubtitleStyle(workbook);
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook, false);
        CellStyle dataStyleAlt = createDataStyle(workbook, true);
        CellStyle centerStyle = createCenterStyle(workbook, false);
        CellStyle centerStyleAlt = createCenterStyle(workbook, true);
        CellStyle scoreStyle = createScoreStyle(workbook, false);
        CellStyle scoreStyleAlt = createScoreStyle(workbook, true);
        CellStyle totalStyle = createTotalStyle(workbook);

        int totalColumns = 5 + masterRounds.size() + 1;

        // Title row
        Row titleRow = summarySheet.createRow(0);
        titleRow.setHeightInPoints(28);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("PRAGMATRIX 2026 — VORTEX MASTER SCORESHEET");
        titleCell.setCellStyle(titleStyle);
        summarySheet.addMergedRegion(new CellRangeAddress(0, 0, 0, totalColumns - 1));

        // Subtitle row
        Row subRow = summarySheet.createRow(1);
        subRow.setHeightInPoints(18);
        Cell subCell = subRow.createCell(0);
        String genTime = new SimpleDateFormat("dd MMMM yyyy, hh:mm a").format(new Date());
        subCell.setCellValue("Generated on: " + genTime + " | Applied Management Carnival, Seshadripuram College");
        subCell.setCellStyle(subtitleStyle);
        summarySheet.addMergedRegion(new CellRangeAddress(1, 1, 0, totalColumns - 1));

        // Blank row
        summarySheet.createRow(2).setHeightInPoints(10);

        // Header row
        Row headerRow = summarySheet.createRow(3);
        headerRow.setHeightInPoints(24);

        int colIdx = 0;
        createCell(headerRow, colIdx++, "#", headerStyle);
        createCell(headerRow, colIdx++, "Team Code", headerStyle);
        createCell(headerRow, colIdx++, "College / Institution", headerStyle);
        createCell(headerRow, colIdx++, "Team Lead Name", headerStyle);
        createCell(headerRow, colIdx++, "Team Lead Email", headerStyle);

        for (Round r : masterRounds) {
            String roundLabel = "Round " + r.getRoundNumber() + " (" + r.getRoundName() + ")";
            createCell(headerRow, colIdx++, roundLabel, headerStyle);
        }
        createCell(headerRow, colIdx++, "Total Score", headerStyle);

        // Map team uniqueId -> (roundId -> Score)
        Map<String, Map<Integer, Score>> teamScoreMap = new HashMap<>();
        for (Team t : teams) {
            teamScoreMap.put(t.getUniqueId(), scoreDAO.findByTeam(t.getUniqueId()));
        }

        // Data rows for summary
        int rowIdx = 4;
        for (int i = 0; i < teams.size(); i++) {
            Team team = teams.get(i);
            boolean isAlt = (i % 2 == 1);
            CellStyle curData = isAlt ? dataStyleAlt : dataStyle;
            CellStyle curCenter = isAlt ? centerStyleAlt : centerStyle;
            CellStyle curScore = isAlt ? scoreStyleAlt : scoreStyle;

            Row row = summarySheet.createRow(rowIdx++);
            row.setHeightInPoints(20);

            colIdx = 0;
            createCell(row, colIdx++, i + 1, curCenter);
            createCell(row, colIdx++, team.getUniqueId(), curCenter);
            createCell(row, colIdx++, team.getCollegeName(), curData);
            createCell(row, colIdx++, team.getTeamLeadName(), curData);
            createCell(row, colIdx++, team.getLeadEmail(), curData);

            Map<Integer, Score> scores = teamScoreMap.getOrDefault(team.getUniqueId(), Collections.emptyMap());
            double total = 0;

            for (Round r : masterRounds) {
                Score s = scores.get(r.getRoundId());
                double pts = (s != null) ? s.getPoints() : 0.0;
                total += pts;
                createCell(row, colIdx++, pts, curScore);
            }

            createCell(row, colIdx++, total, totalStyle);
        }

        // Auto-fit columns
        for (int c = 0; c < totalColumns; c++) {
            summarySheet.autoSizeColumn(c);
            int width = summarySheet.getColumnWidth(c);
            summarySheet.setColumnWidth(c, Math.max(width + 1200, 3200));
        }

        // 2. INDIVIDUAL ROUND BREAKDOWN SHEETS
        for (VortexRound vr : vortexRounds) {
            String sheetTitle = "R" + vr.getDisplayOrder() + " - " + vr.getRoundName();
            if (sheetTitle.length() > 31) {
                sheetTitle = sheetTitle.substring(0, 31);
            }
            Sheet roundSheet = workbook.createSheet(sheetTitle);
            roundSheet.setDisplayGridlines(true);

            List<JudgingComponent> components = vr.getComponents();
            List<JudgingCriterion> allCriteria = new ArrayList<>();
            for (JudgingComponent comp : components) {
                allCriteria.addAll(comp.getCriteria());
            }

            int roundCols = 4 + allCriteria.size() + 1; // #, Code, College, Lead, [Criteria...], Round Total

            // Title row
            Row rTitleRow = roundSheet.createRow(0);
            rTitleRow.setHeightInPoints(26);
            Cell rTitleCell = rTitleRow.createCell(0);
            rTitleCell.setCellValue("VORTEX — " + vr.getRoundName().toUpperCase() + " DETAILED BREAKDOWN");
            rTitleCell.setCellStyle(titleStyle);
            roundSheet.addMergedRegion(new CellRangeAddress(0, 0, 0, roundCols - 1));

            // Blank row
            roundSheet.createRow(1).setHeightInPoints(8);

            // Header row
            Row rHeaderRow = roundSheet.createRow(2);
            rHeaderRow.setHeightInPoints(26);

            int rCol = 0;
            createCell(rHeaderRow, rCol++, "#", headerStyle);
            createCell(rHeaderRow, rCol++, "Team Code", headerStyle);
            createCell(rHeaderRow, rCol++, "College", headerStyle);
            createCell(rHeaderRow, rCol++, "Team Lead", headerStyle);

            for (JudgingCriterion crit : allCriteria) {
                String critLabel = crit.getCriterionName() + " (" + crit.getMaxMarks() + " pts)";
                createCell(rHeaderRow, rCol++, critLabel, headerStyle);
            }
            createCell(rHeaderRow, rCol++, "Round Total", headerStyle);

            // Data rows
            int rRowIdx = 3;
            for (int i = 0; i < teams.size(); i++) {
                Team team = teams.get(i);
                boolean isAlt = (i % 2 == 1);
                CellStyle curData = isAlt ? dataStyleAlt : dataStyle;
                CellStyle curCenter = isAlt ? centerStyleAlt : centerStyle;
                CellStyle curScore = isAlt ? scoreStyleAlt : scoreStyle;

                Row row = roundSheet.createRow(rRowIdx++);
                row.setHeightInPoints(20);

                rCol = 0;
                createCell(row, rCol++, i + 1, curCenter);
                createCell(row, rCol++, team.getUniqueId(), curCenter);
                createCell(row, rCol++, team.getCollegeName(), curData);
                createCell(row, rCol++, team.getTeamLeadName(), curData);

                Map<Integer, Double> critScores = vortexCriteriaDAO.getTeamScoresForRound(team.getUniqueId(), vr.getRoundId());
                double rTotal = 0;

                for (JudgingCriterion crit : allCriteria) {
                    Double scoreVal = critScores.get(crit.getCriterionId());
                    double pts = (scoreVal != null) ? scoreVal : 0.0;
                    rTotal += pts;
                    createCell(row, rCol++, pts, curScore);
                }

                createCell(row, rCol++, rTotal, totalStyle);
            }

            for (int c = 0; c < roundCols; c++) {
                roundSheet.autoSizeColumn(c);
                int width = roundSheet.getColumnWidth(c);
                roundSheet.setColumnWidth(c, Math.max(width + 1000, 3000));
            }
        }
    }

    private void createCell(Row row, int column, String value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value != null ? value : "");
        cell.setCellStyle(style);
    }

    private void createCell(Row row, int column, double value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private void createCell(Row row, int column, int value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private CellStyle createTitleStyle(XSSFWorkbook workbook, String quizName) {
        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontName("Calibri");
        font.setFontHeightInPoints((short) 15);
        font.setBold(true);
        font.setColor(new XSSFColor(new byte[]{(byte) 26, (byte) 26, (byte) 46}, null)); // #1A1A2E
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createSubtitleStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontName("Calibri");
        font.setFontHeightInPoints((short) 10);
        font.setItalic(true);
        font.setColor(new XSSFColor(new byte[]{(byte) 100, (byte) 100, (byte) 100}, null));
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createHeaderStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontName("Calibri");
        font.setFontHeightInPoints((short) 11);
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);

        style.setFillForegroundColor(new XSSFColor(new byte[]{(byte) 27, (byte) 31, (byte) 59}, null)); // Dark Navy #1B1F3B
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorders(style, BorderStyle.THIN, IndexedColors.GREY_40_PERCENT.getIndex());
        return style;
    }

    private CellStyle createDataStyle(XSSFWorkbook workbook, boolean isAlt) {
        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontName("Calibri");
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        if (isAlt) {
            style.setFillForegroundColor(new XSSFColor(new byte[]{(byte) 248, (byte) 249, (byte) 250}, null));
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        }
        setBorders(style, BorderStyle.THIN, IndexedColors.GREY_25_PERCENT.getIndex());
        return style;
    }

    private CellStyle createCenterStyle(XSSFWorkbook workbook, boolean isAlt) {
        CellStyle style = createDataStyle(workbook, isAlt);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle createScoreStyle(XSSFWorkbook workbook, boolean isAlt) {
        CellStyle style = createDataStyle(workbook, isAlt);
        style.setAlignment(HorizontalAlignment.RIGHT);
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("0.00"));
        return style;
    }

    private CellStyle createTotalStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontName("Calibri");
        font.setFontHeightInPoints((short) 11);
        font.setBold(true);
        font.setColor(new XSSFColor(new byte[]{(byte) 74, (byte) 21, (byte) 75}, null)); // Deep Purple #4A154B
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(new XSSFColor(new byte[]{(byte) 254, (byte) 249, (byte) 231}, null)); // Gold tint #FEF9E7
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("0.00"));
        setBorders(style, BorderStyle.THIN, IndexedColors.GREY_40_PERCENT.getIndex());
        return style;
    }

    private void setBorders(CellStyle style, BorderStyle borderStyle, short colorIndex) {
        style.setBorderTop(borderStyle);
        style.setBorderBottom(borderStyle);
        style.setBorderLeft(borderStyle);
        style.setBorderRight(borderStyle);
        style.setTopBorderColor(colorIndex);
        style.setBottomBorderColor(colorIndex);
        style.setLeftBorderColor(colorIndex);
        style.setRightBorderColor(colorIndex);
    }
}
