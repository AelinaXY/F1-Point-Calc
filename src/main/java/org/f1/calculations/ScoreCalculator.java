package org.f1.calculations;

import org.f1.domain.FullPointEntity;

public class ScoreCalculator {

    private static Double averagePointWeight = 0.5;
    private static Double threeAveragePointWeight = 0.5;

    public static Double calculateScore(FullPointEntity fullPointEntity) {
        Double runningTotal = 0.0;

        runningTotal += fullPointEntity.getUpdatedAveragePoints() * averagePointWeight;

        runningTotal += fullPointEntity.getUpdatedThreeRaceAveragePoints() * threeAveragePointWeight;

        return runningTotal;
    }
}
