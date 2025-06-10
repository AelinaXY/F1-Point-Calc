package org.f1.calculations;

import org.f1.domain.FullPointEntity;

public class ScoreCalculator {

    private static Double averagePointWeight = 0.65;
    private static Double threeAveragePointWeight = 0.35;

    public static Double calculateScore(FullPointEntity fullPointEntity) {
        double runningTotal = 0.0;

        runningTotal += fullPointEntity.getUpdatedAveragePoints() * averagePointWeight;

        runningTotal += fullPointEntity.getUpdatedThreeRaceAveragePoints() * threeAveragePointWeight;

        return runningTotal;
    }

    public static void setAveragePointWeight(Double averagePointWeight) {
        ScoreCalculator.averagePointWeight = averagePointWeight;
    }

    public static void setThreeAveragePointWeight(Double threeAveragePointWeight) {
        ScoreCalculator.threeAveragePointWeight = threeAveragePointWeight;
    }
}
