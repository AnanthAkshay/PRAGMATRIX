package com.pragmatrix.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class JudgingComponent implements Serializable {
    private static final long serialVersionUID = 1L;

    private int componentId;
    private int roundId;
    private String componentLabel;
    private int maxMarks;
    private int displayOrder;
    private List<JudgingCriterion> criteria = new ArrayList<>();

    public JudgingComponent() {}

    public JudgingComponent(int componentId, int roundId, String componentLabel, int maxMarks, int displayOrder) {
        this.componentId = componentId;
        this.roundId = roundId;
        this.componentLabel = componentLabel;
        this.maxMarks = maxMarks;
        this.displayOrder = displayOrder;
    }

    public int getComponentId() { return componentId; }
    public void setComponentId(int componentId) { this.componentId = componentId; }

    public int getRoundId() { return roundId; }
    public void setRoundId(int roundId) { this.roundId = roundId; }

    public String getComponentLabel() { return componentLabel; }
    public void setComponentLabel(String componentLabel) { this.componentLabel = componentLabel; }

    public int getMaxMarks() { return maxMarks; }
    public void setMaxMarks(int maxMarks) { this.maxMarks = maxMarks; }

    public int getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(int displayOrder) { this.displayOrder = displayOrder; }

    public List<JudgingCriterion> getCriteria() { return criteria; }
    public void setCriteria(List<JudgingCriterion> criteria) { this.criteria = criteria; }

    public int getCalculatedMaxMarks() {
        int sum = 0;
        for (JudgingCriterion c : criteria) {
            sum += c.getMaxMarks();
        }
        return sum;
    }
}
