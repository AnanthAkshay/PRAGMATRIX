package com.pragmatrix.model;

import java.sql.Timestamp;

/**
 * Represents a registered team.
 */
public class Team {
    private String uniqueId;
    private String quizCode;
    private String collegeName;
    private String teamLeadName;
    private String leadEmail;
    private String member2Name;
    private String member3Name;
    private Timestamp registeredAt;

    private boolean eliminated;
    private boolean advancedToFinale;

    /** Transient field: total points (populated by leaderboard queries). */
    private double totalPoints;

    public Team() {}

    public Team(String quizCode, String collegeName, String teamLeadName, String leadEmail) {
        this.quizCode = quizCode;
        this.collegeName = collegeName;
        this.teamLeadName = teamLeadName;
        this.leadEmail = leadEmail;
    }

    public Team(String quizCode, String collegeName, String teamLeadName, String leadEmail, String member2Name, String member3Name) {
        this.quizCode = quizCode;
        this.collegeName = collegeName;
        this.teamLeadName = teamLeadName;
        this.leadEmail = leadEmail;
        this.member2Name = member2Name;
        this.member3Name = member3Name;
    }

    public String getUniqueId() { return uniqueId; }
    public void setUniqueId(String uniqueId) { this.uniqueId = uniqueId; }

    public String getQuizCode() { return quizCode; }
    public void setQuizCode(String quizCode) { this.quizCode = quizCode; }

    public String getCollegeName() { return collegeName; }
    public void setCollegeName(String collegeName) { this.collegeName = collegeName; }

    public String getTeamLeadName() { return teamLeadName; }
    public void setTeamLeadName(String teamLeadName) { this.teamLeadName = teamLeadName; }

    public String getLeadEmail() { return leadEmail; }
    public void setLeadEmail(String leadEmail) { this.leadEmail = leadEmail; }

    public String getMember2Name() { return member2Name; }
    public void setMember2Name(String member2Name) { this.member2Name = member2Name; }

    public String getMember3Name() { return member3Name; }
    public void setMember3Name(String member3Name) { this.member3Name = member3Name; }

    public boolean isEliminated() { return eliminated; }
    public void setEliminated(boolean eliminated) { this.eliminated = eliminated; }

    public boolean isAdvancedToFinale() { return advancedToFinale; }
    public void setAdvancedToFinale(boolean advancedToFinale) { this.advancedToFinale = advancedToFinale; }

    public Timestamp getRegisteredAt() { return registeredAt; }
    public void setRegisteredAt(Timestamp registeredAt) { this.registeredAt = registeredAt; }

    public double getTotalPoints() { return totalPoints; }
    public void setTotalPoints(double totalPoints) { this.totalPoints = totalPoints; }
}
