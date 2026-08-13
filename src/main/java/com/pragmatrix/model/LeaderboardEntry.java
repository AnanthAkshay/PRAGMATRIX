package com.pragmatrix.model;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents a row in the leaderboard — a team with aggregated scores.
 * Not directly mapped to a single table; populated from the leaderboard VIEW
 * or a custom query joining teams, scores, and rounds.
 */
public class LeaderboardEntry {
    private String uniqueId;
    private String collegeName;
    private String student1Name;
    private String student2Name;
    private String student3Name;
    private String quizCode;
    private double totalPoints;

    /** Map of roundNumber → points for that round (null/missing means round not finished or no score). */
    private Map<Integer, Double> roundPoints = new LinkedHashMap<>();

    public LeaderboardEntry() {}

    public String getUniqueId() { return uniqueId; }
    public void setUniqueId(String uniqueId) { this.uniqueId = uniqueId; }

    public String getCollegeName() { return collegeName; }
    public void setCollegeName(String collegeName) { this.collegeName = collegeName; }

    public String getStudent1Name() { return student1Name; }
    public void setStudent1Name(String student1Name) { this.student1Name = student1Name; }

    public String getStudent2Name() { return student2Name; }
    public void setStudent2Name(String student2Name) { this.student2Name = student2Name; }

    public String getStudent3Name() { return student3Name; }
    public void setStudent3Name(String student3Name) { this.student3Name = student3Name; }

    public String getQuizCode() { return quizCode; }
    public void setQuizCode(String quizCode) { this.quizCode = quizCode; }

    public double getTotalPoints() { return totalPoints; }
    public void setTotalPoints(double totalPoints) { this.totalPoints = totalPoints; }

    public Map<Integer, Double> getRoundPoints() { return roundPoints; }
    public void setRoundPoints(Map<Integer, Double> roundPoints) { this.roundPoints = roundPoints; }

    public void putRoundPoints(int roundNumber, double points) {
        this.roundPoints.put(roundNumber, points);
    }
}
