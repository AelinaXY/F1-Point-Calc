package org.f1.calculations;

import org.f1.domain.FullPointEntity;

public class ScoreCalculator {

    private static Double averagePointWeight = 0.635;
    private static Double threeAveragePointWeight = 0.28;
    private static Double simplePredictedPointsWeight = 0.085;


    public static Double calculateScore(FullPointEntity fullPointEntity) {
        double runningTotal = 0.0;

        runningTotal += fullPointEntity.getUpdatedAveragePoints() * averagePointWeight;

        runningTotal += fullPointEntity.getUpdatedThreeRaceAveragePoints() * threeAveragePointWeight;

        if(fullPointEntity.getSimplePredictedPoints() != null)
        {
            runningTotal += fullPointEntity.getSimplePredictedPoints() * simplePredictedPointsWeight;
        }
        else
        {
            runningTotal += fullPointEntity.getUpdatedAveragePoints() * simplePredictedPointsWeight*(averagePointWeight/(averagePointWeight+threeAveragePointWeight));

            runningTotal += fullPointEntity.getUpdatedThreeRaceAveragePoints() * simplePredictedPointsWeight*(threeAveragePointWeight/(averagePointWeight+threeAveragePointWeight));
        }

        return runningTotal;
    }

    public static void setAveragePointWeight(Double averagePointWeight) {
        ScoreCalculator.averagePointWeight = averagePointWeight;
    }

    public static void setThreeAveragePointWeight(Double threeAveragePointWeight) {
        ScoreCalculator.threeAveragePointWeight = threeAveragePointWeight;
    }

    public static void setSimplePredictedPointsWeight(Double simplePredictedPointsWeight) {
        ScoreCalculator.simplePredictedPointsWeight = simplePredictedPointsWeight;
    }
}
