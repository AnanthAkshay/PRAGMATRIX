package com.pragmatrix.model;

import java.io.Serializable;

public class JudgingCriterion implements Serializable {
    private static final long serialVersionUID = 1L;

    private int criterionId;
    private int componentId;
    private String criterionName;
    private String judgesLookFor;
    private int maxMarks;
    private int displayOrder;

    public JudgingCriterion() {}

    public JudgingCriterion(int criterionId, int componentId, String criterionName, String judgesLookFor, int maxMarks, int displayOrder) {
        this.criterionId = criterionId;
        this.componentId = componentId;
        this.criterionName = criterionName;
        this.judgesLookFor = judgesLookFor;
        this.maxMarks = maxMarks;
        this.displayOrder = displayOrder;
    }

    public int getCriterionId() { return criterionId; }
    public void setCriterionId(int criterionId) { this.criterionId = criterionId; }

    public int getComponentId() { return componentId; }
    public void setComponentId(int componentId) { this.componentId = componentId; }

    public String getCriterionName() { return criterionName; }
    public void setCriterionName(String criterionName) { this.criterionName = criterionName; }

    public String getJudgesLookFor() { return judgesLookFor; }
    public void setJudgesLookFor(String judgesLookFor) { this.judgesLookFor = judgesLookFor; }

    public int getMaxMarks() { return maxMarks; }
    public void setMaxMarks(int maxMarks) { this.maxMarks = maxMarks; }

    public int getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(int displayOrder) { this.displayOrder = displayOrder; }
}
