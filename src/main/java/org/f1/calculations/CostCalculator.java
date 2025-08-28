package org.f1.calculations;

import org.f1.domain.FullPointEntity;
import org.f1.domain.Race;

import java.util.ArrayList;
import java.util.List;

public class CostCalculator {

    public static final double TERRIBLE = 0.605;
    public static final double POOR = 0.9;
    public static final double GOOD = 1.195;

    public static Double calculateCostChange(FullPointEntity fullPointEntity, String raceName, boolean isSprint, ScoreCalculatorInterface calculator) {
        Double currentCost = fullPointEntity.getCost();

        List<Double> scoreList = calculateRaceList(fullPointEntity, raceName, isSprint, calculator);

        if (scoreList.isEmpty()) {
            return 0d;
        }

        Double averageResult = scoreList.stream()
                .reduce(0d, Double::sum) / scoreList.size();

        if (currentCost >= 18.5d) {
            return calculateChange(averageResult, currentCost, 0.3d);
        }
        return calculateChange(averageResult, currentCost, 0.6d);

    }

    private static Double calculateChange(Double averageResult, Double currentCost, Double range) {
        double budgetScore = averageResult / currentCost;

       if(budgetScore < TERRIBLE)
       {
           return -range;
       }
       if(budgetScore < POOR)
       {
           return -(range/3);
       }
       if(budgetScore < GOOD)
       {
           return range/3;
       }
       return range;

    }

    private static List<Double> calculateRaceList(FullPointEntity fullPointEntity, String raceName, boolean isSprint, ScoreCalculatorInterface calculator) {
        List<Double> iterableRaceList = new ArrayList<>();

        for (Race race : fullPointEntity.getRaceList()) {
            if (race.name().equals(raceName)) {
                break;
            }
            iterableRaceList.add(race.totalPoints());

            if (iterableRaceList.size() > 3) {
                iterableRaceList.removeFirst();
            }
        }
        iterableRaceList.add(calculator.calculateScore(fullPointEntity,raceName,isSprint));
        iterableRaceList.removeFirst();

        return iterableRaceList;
    }
}
