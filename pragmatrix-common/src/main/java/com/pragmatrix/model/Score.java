package com.pragmatrix.model;

import java.sql.Timestamp;

/**
 * Represents a score entry for one team in one round.
 */
public class Score {
    private int scoreId;
    private String uniqueId;
    private int roundId;
    private double points;
    private int enteredBy;
    private Timestamp enteredAt;

    public Score() {}

    public Score(String uniqueId, int roundId, double points, int enteredBy) {
        this.uniqueId = uniqueId;
        this.roundId = roundId;
        this.points = points;
        this.enteredBy = enteredBy;
    }

    public int getScoreId() { return scoreId; }
    public void setScoreId(int scoreId) { this.scoreId = scoreId; }

    public String getUniqueId() { return uniqueId; }
    public void setUniqueId(String uniqueId) { this.uniqueId = uniqueId; }

    public int getRoundId() { return roundId; }
    public void setRoundId(int roundId) { this.roundId = roundId; }

    public double getPoints() { return points; }
    public void setPoints(double points) { this.points = points; }

    public int getEnteredBy() { return enteredBy; }
    public void setEnteredBy(int enteredBy) { this.enteredBy = enteredBy; }

    public Timestamp getEnteredAt() { return enteredAt; }
    public void setEnteredAt(Timestamp enteredAt) { this.enteredAt = enteredAt; }
}
