package org.f1.agents;

import org.f1.domain.ScoreCard;

import java.util.SequencedMap;

public class CostCapMultAgent extends BaseAgent {

    private double costCapMultiplier;

    public CostCapMultAgent(double costCapMultiplier) {
        super();
        this.costCapMultiplier = costCapMultiplier;
    }

    public CostCapMultAgent(ScoreCard currentScoreCard, SequencedMap<String, ScoreCard> previousScoreCards, Double costCap, Double score, Double costCapMultiplier) {
        super(currentScoreCard, previousScoreCards, costCap, score);
        this.costCapMultiplier = costCapMultiplier;
    }

    public double getCostCapMultiplier() {
        return costCapMultiplier;
    }

    public void setCostCapMultiplier(double costCapMultiplier) {
        this.costCapMultiplier = costCapMultiplier;
    }

    @Override
    public String toString() {
        return "CostCapMultAgent{" +
                "costCapMultiplier=" + costCapMultiplier +
                "} " + super.toString();
    }
}
