package org.f1.calculations;

import org.f1.domain.FullPointEntity;
import org.f1.domain.Race;
import org.f1.domain.ScoreCard;
import org.f1.domain.SquaredErrorValue;
import org.f1.parsing.CSVParsing;

import java.util.*;
import java.util.stream.Collectors;

public class RegressionDataCalculations extends AbstractCalculation {


    public RegressionDataCalculations(Set<FullPointEntity> driverSet, Set<FullPointEntity> teamSet, double costCap, long transferLimit) {
        super(driverSet, teamSet, costCap, transferLimit);
    }


    //TEST sMAPE and MSE_log
    public Double calculateErrorValue(FullPointEntity entity, String race) {
        List<Race> raceList = entity.getRaceList();
        Optional<Race> actualRace = raceList.stream().filter(race1 -> race1.name().equals(race)).findFirst();

        if (actualRace.isPresent()) {
            entity.calculateUpdatedPoints(race);
            Double expectedScore = ScoreCalculator.calculateScore(entity);
            Double actualScore = actualRace.get().totalPoints();
            return expectedScore - actualScore;
        }
        return Double.NaN;
    }

    public Map<String, SquaredErrorValue> calculateMeanSquaredErrorValue(Set<Set<FullPointEntity>> entitySets) {
        Set<FullPointEntity> entitySet = entitySets.stream().flatMap(Set::stream).collect(Collectors.toSet());
        Map<String, SquaredErrorValue> meanSquaredErrorMap = new HashMap<>();

        for (FullPointEntity entity : entitySet) {
            List<Race> raceList = new ArrayList<>(entity.getRaceList());
            raceList.removeFirst();
            for (Race race : raceList) {
                Double errorValue = calculateErrorValue(entity, race.name());
                meanSquaredErrorMap.compute(race.name(), (k, v) -> v == null ? new SquaredErrorValue(errorValue) : v.increment(errorValue));
            }
        }
        return meanSquaredErrorMap;
    }

    public void regressionCalculation() {
        Set<FullPointEntity> drivers2024 = CSVParsing.fullParse("Drivers_Full_2024.csv");
        Set<FullPointEntity> teams2024 = CSVParsing.fullParse("Teams_Full_2024.csv");

        Set<Set<FullPointEntity>> pointEntitySets = Set.of(getDriverSet(), getTeamSet(), drivers2024, teams2024);

        Map<List<Double>, Double> scoreWeightMap = new HashMap<>();

        Set<List<Double>> weightSet = new HashSet<>();

//        for (double i = 0.0; i <= 1; i += 0.01) {
//            for (double j = 0.0; j <= 1-i; j += 0.01) {
//                weightSet.add(List.of(i, j,1-i-j));
//            }
//        }
        for (double i = 0.0; i <= 1; i += 0.01) {
            weightSet.add(List.of(i, 1 - i));
        }

        for (List<Double> weights : weightSet) {
            ScoreCalculator.setAveragePointWeight(weights.get(0));
            ScoreCalculator.setThreeAveragePointWeight(weights.get(1));
//            ScoreCalculator.setSimplePredictedPointsWeight(weights.get(2));
            Map<String, SquaredErrorValue> squaredErrorValueMap = calculateMeanSquaredErrorValue(pointEntitySets);

            Double meanSquaredError = 0.0;
            int count = 0;
            for (Map.Entry<String, SquaredErrorValue> entry : squaredErrorValueMap.entrySet()) {
                meanSquaredError += entry.getValue().getValue();
                count += entry.getValue().getCount();
            }
            scoreWeightMap.put(weights, meanSquaredError / count);
        }

        scoreWeightMap.entrySet().stream().sorted(Map.Entry.comparingByValue()).forEach(System.out::println);

    }

    //TODO:
    //Method 2: Take in a Set<FullPointEntity> and calculate the mean square error for each race prediction using the scoring algo
    //Output: Set of mean square errors

    //detect outliers?


    //Method 3: Iterate on method 2 to find the lowest error value for weight combinations for scoring algorithm
    //Figure out how to optimally calculate the weight combination
    //Linear optimization


}
