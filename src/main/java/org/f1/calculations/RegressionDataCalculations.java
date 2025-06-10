package org.f1.calculations;

import org.f1.domain.FullPointEntity;
import org.f1.domain.Race;
import org.f1.domain.SquaredErrorValue;

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
        Set<Set<FullPointEntity>> pointEntitySets = Set.of(getDriverSet(), getTeamSet());

        Map<String, SquaredErrorValue> squaredErrorValueMap = calculateMeanSquaredErrorValue(pointEntitySets);

        for (Map.Entry<String, SquaredErrorValue> entry : squaredErrorValueMap.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
    }

    //TODO:
    //Method 2: Take in a Set<FullPointEntity> and calculate the mean square error for each race prediction using the scoring algo
    //Output: Set of mean square errors

    //detect outlayers?


    //Method 3: Iterate on method 2 to find the lowest error value for weight combinations for scoring algorithm
    //Figure out how to optimally calculate the weight combination
    //Linear optimization


}
