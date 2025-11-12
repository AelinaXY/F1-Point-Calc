package org.f1.calculations;

import org.f1.domain.FullPointEntity;
import org.f1.domain.Race;

public class ActualScoreCalculator implements ScoreCalculatorInterface {

    public ActualScoreCalculator() {
    }

    @Override
    public Double calculateScore(FullPointEntity fullPointEntity, String raceName, boolean isSprint) {

        Double score = fullPointEntity.getRaceList().stream().filter(r -> r.name().equals(raceName)).map(Race::totalPoints).findFirst().orElse(null);

        if(score == null)
        {
            return 0d;
        }
        return score;
    }
}
