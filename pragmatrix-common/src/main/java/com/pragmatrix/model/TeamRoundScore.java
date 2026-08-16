package com.pragmatrix.model;

import java.io.Serializable;
import java.sql.Timestamp;

public class TeamRoundScore implements Serializable {
    private static final long serialVersionUID = 1L;

    private int scoreId;
    private String uniqueId;
    private int criterionId;
    private double scoreAwarded;
    private String evaluatorName;
    private Timestamp dateEvaluated;

    public TeamRoundScore() {}

    public TeamRoundScore(int scoreId, String uniqueId, int criterionId, double scoreAwarded, String evaluatorName, Timestamp dateEvaluated) {
        this.scoreId = scoreId;
        this.uniqueId = uniqueId;
        this.criterionId = criterionId;
        this.scoreAwarded = scoreAwarded;
        this.evaluatorName = evaluatorName;
        this.dateEvaluated = dateEvaluated;
    }

    public int getScoreId() { return scoreId; }
    public void setScoreId(int scoreId) { this.scoreId = scoreId; }

    public String getUniqueId() { return uniqueId; }
    public void setUniqueId(String uniqueId) { this.uniqueId = uniqueId; }

    public int getCriterionId() { return criterionId; }
    public void setCriterionId(int criterionId) { this.criterionId = criterionId; }

    public double getScoreAwarded() { return scoreAwarded; }
    public void setScoreAwarded(double scoreAwarded) { this.scoreAwarded = scoreAwarded; }

    public String getEvaluatorName() { return evaluatorName; }
    public void setEvaluatorName(String evaluatorName) { this.evaluatorName = evaluatorName; }

    public Timestamp getDateEvaluated() { return dateEvaluated; }
    public void setDateEvaluated(Timestamp dateEvaluated) { this.dateEvaluated = dateEvaluated; }
}
