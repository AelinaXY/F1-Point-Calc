package org.f1.agents;

import org.f1.domain.ScoreCard;

import java.util.LinkedHashMap;
import java.util.SequencedMap;

public class BaseAgent {
    private ScoreCard currentScoreCard;
    private SequencedMap<String, ScoreCard> previousScoreCards;
    private Double costCap;
    private Double score;

    public BaseAgent(ScoreCard currentScoreCard, SequencedMap<String, ScoreCard> previousScoreCards, Double costCap, Double score) {
        this.currentScoreCard = currentScoreCard;
        this.previousScoreCards = previousScoreCards;
        this.costCap = costCap;
        this.score = score;
    }

    public BaseAgent() {
        previousScoreCards = new LinkedHashMap<>();
    }

    public void addScoreCard(String raceName, ScoreCard scoreCard) {
        previousScoreCards.put(raceName, scoreCard);
    }

    public void addScore(Double score) {
        this.score += score;
    }

    public void addCostCap(Double costCap) {
        this.costCap += costCap;
    }

    public ScoreCard getCurrentScoreCard() {
        return currentScoreCard;
    }

    public void setCurrentScoreCard(ScoreCard currentScoreCard) {
        this.currentScoreCard = currentScoreCard;
    }

    public SequencedMap<String, ScoreCard> getPreviousScoreCards() {
        return previousScoreCards;
    }

    public void setPreviousScoreCards(SequencedMap<String, ScoreCard> previousScoreCards) {
        this.previousScoreCards = previousScoreCards;
    }

    public Double getCostCap() {
        return costCap;
    }

    public void setCostCap(Double costCap) {
        this.costCap = costCap;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return "BaseAgent{" +
                "score=" + score +
                ", costCap=" + costCap +
                '}';
    }
}
