package com.pragmatrix.model;

import java.sql.Timestamp;

/**
 * Represents a quiz round.
 */
public class Round {
    private int roundId;
    private String quizCode;
    private int roundNumber;
    private String roundName;
    private String judgingCriteria;
    private boolean isFinished;
    private Timestamp finishedAt;

    public Round() {}

    public int getRoundId() { return roundId; }
    public void setRoundId(int roundId) { this.roundId = roundId; }

    public String getQuizCode() { return quizCode; }
    public void setQuizCode(String quizCode) { this.quizCode = quizCode; }

    public int getRoundNumber() { return roundNumber; }
    public void setRoundNumber(int roundNumber) { this.roundNumber = roundNumber; }

    public String getRoundName() { return roundName; }
    public void setRoundName(String roundName) { this.roundName = roundName; }

    public String getJudgingCriteria() { return judgingCriteria; }
    public void setJudgingCriteria(String judgingCriteria) { this.judgingCriteria = judgingCriteria; }

    public boolean isFinished() { return isFinished; }
    public void setFinished(boolean finished) { isFinished = finished; }

    public Timestamp getFinishedAt() { return finishedAt; }
    public void setFinishedAt(Timestamp finishedAt) { this.finishedAt = finishedAt; }
}
