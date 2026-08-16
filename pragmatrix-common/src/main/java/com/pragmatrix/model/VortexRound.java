package com.pragmatrix.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class VortexRound implements Serializable {
    private static final long serialVersionUID = 1L;

    private int roundId;
    private String roundName;
    private int displayOrder;
    private List<JudgingComponent> components = new ArrayList<>();

    public VortexRound() {}

    public VortexRound(int roundId, String roundName, int displayOrder) {
        this.roundId = roundId;
        this.roundName = roundName;
        this.displayOrder = displayOrder;
    }

    public int getRoundId() { return roundId; }
    public void setRoundId(int roundId) { this.roundId = roundId; }

    public String getRoundName() { return roundName; }
    public void setRoundName(String roundName) { this.roundName = roundName; }

    public int getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(int displayOrder) { this.displayOrder = displayOrder; }

    public List<JudgingComponent> getComponents() { return components; }
    public void setComponents(List<JudgingComponent> components) { this.components = components; }

    public int getTotalMaxMarks() {
        int total = 0;
        for (JudgingComponent comp : components) {
            total += comp.getMaxMarks();
        }
        return total;
    }
}
