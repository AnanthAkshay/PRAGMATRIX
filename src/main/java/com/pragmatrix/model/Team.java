package com.pragmatrix.model;

import java.sql.Timestamp;

/**
 * Represents a registered team.
 */
public class Team {
    private String uniqueId;
    private String quizCode;
    private String collegeName;
    private String leadEmail;
    private String student1Name;
    private String student2Name;
    private String student3Name;
    private Timestamp registeredAt;

    /** Transient field: total points (populated by leaderboard queries). */
    private double totalPoints;

    public Team() {}

    public Team(String quizCode, String collegeName, String leadEmail,
                String student1Name, String student2Name, String student3Name) {
        this.quizCode = quizCode;
        this.collegeName = collegeName;
        this.leadEmail = leadEmail;
        this.student1Name = student1Name;
        this.student2Name = student2Name;
        this.student3Name = student3Name;
    }

    public String getUniqueId() { return uniqueId; }
    public void setUniqueId(String uniqueId) { this.uniqueId = uniqueId; }

    public String getQuizCode() { return quizCode; }
    public void setQuizCode(String quizCode) { this.quizCode = quizCode; }

    public String getCollegeName() { return collegeName; }
    public void setCollegeName(String collegeName) { this.collegeName = collegeName; }

    public String getLeadEmail() { return leadEmail; }
    public void setLeadEmail(String leadEmail) { this.leadEmail = leadEmail; }

    public String getStudent1Name() { return student1Name; }
    public void setStudent1Name(String student1Name) { this.student1Name = student1Name; }

    public String getStudent2Name() { return student2Name; }
    public void setStudent2Name(String student2Name) { this.student2Name = student2Name; }

    public String getStudent3Name() { return student3Name; }
    public void setStudent3Name(String student3Name) { this.student3Name = student3Name; }

    public Timestamp getRegisteredAt() { return registeredAt; }
    public void setRegisteredAt(Timestamp registeredAt) { this.registeredAt = registeredAt; }

    public double getTotalPoints() { return totalPoints; }
    public void setTotalPoints(double totalPoints) { this.totalPoints = totalPoints; }
}
