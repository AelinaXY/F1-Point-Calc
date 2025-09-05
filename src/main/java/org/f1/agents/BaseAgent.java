package org.f1.agents;

import org.f1.domain.ScoreCard;

import java.util.SortedMap;

public class BaseAgent {

    private ScoreCard currentScoreCard;
    private SortedMap<String, ScoreCard> previousScoreCards;
    private Double costCap;
    private Double score;

}
