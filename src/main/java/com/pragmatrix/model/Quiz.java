package com.pragmatrix.model;

/**
 * Represents a quiz event (BIZWIZX or VORTEX).
 */
public class Quiz {
    private String quizCode;
    private String quizName;
    private String idPrefix;

    public Quiz() {}

    public Quiz(String quizCode, String quizName, String idPrefix) {
        this.quizCode = quizCode;
        this.quizName = quizName;
        this.idPrefix = idPrefix;
    }

    public String getQuizCode() { return quizCode; }
    public void setQuizCode(String quizCode) { this.quizCode = quizCode; }

    public String getQuizName() { return quizName; }
    public void setQuizName(String quizName) { this.quizName = quizName; }

    public String getIdPrefix() { return idPrefix; }
    public void setIdPrefix(String idPrefix) { this.idPrefix = idPrefix; }
}
