package org.f1.calculations;

import org.f1.domain.FullPointEntity;
import org.f1.domain.Race;
import org.f1.domain.SquaredErrorValue;
import org.f1.parsing.CSVParsing;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import java.util.stream.Gatherers;

public class RegressionDataCalculation extends AbstractCalculation {


    public RegressionDataCalculation(Set<FullPointEntity> driverSet, Set<FullPointEntity> teamSet, double costCap, long transferLimit, String raceName) {
        super(driverSet, teamSet, costCap, transferLimit, raceName);
    }


    //TEST sMAPE and MSE_log
    public Double calculateErrorValue(FullPointEntity entity, String race) {
        List<Race> raceList = entity.getRaceList();
        Optional<Race> actualRace = raceList.stream().filter(race1 -> race1.name().equals(race)).findFirst();

        if (actualRace.isPresent()) {
            Double expectedScore = ScoreCalculator.calculateScore(entity, race);
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
                meanSquaredErrorMap.compute(race.name(), (_, v) -> v == null ? new SquaredErrorValue(errorValue) : v.increment(errorValue));
            }
        }
        return meanSquaredErrorMap;
    }

    public void regressionCalculation() {
        Set<FullPointEntity> drivers2024 = CSVParsing.parseFullPointEntities("Drivers_Full_2024.csv");
        Set<FullPointEntity> teams2024 = CSVParsing.parseFullPointEntities("Teams_Full_2024.csv");

        Set<Set<FullPointEntity>> pointEntitySets = Set.of(getDriverSet(), getTeamSet(), drivers2024, teams2024);

//        for (double i = 0.0; i <= 1; i += 0.01) {
//            for (double j = 0.0; j <= 1 - i; j += 0.01) {
//                weightSet.add(List.of(i, j, 1 - i - j));
//            }
//        }

//        for (double i = 0.0; i <= 2; i += 0.01) {
//            weightSet.add(List.of(i));
//        }

        List<Double> baseWeights = new ArrayList<>(List.of(1d, 1d, 1d, 1d, 1d, 1d, 1d, 1d));
        int baseIndex = 0;

        for (int i = 0; i < 100; i++) {
            Set<List<Double>> weightSet = new HashSet<>();
            ConcurrentMap<List<Double>, Double> scoreWeightMap = new ConcurrentHashMap<>();

            double valueToIterate = baseWeights.get(baseIndex);
            for (double j = valueToIterate - 5; j <= valueToIterate + 5; j += 0.02) {
                List<Double> newWeights = new ArrayList<>(baseWeights);
                newWeights.set(baseIndex, j);
                weightSet.add(newWeights);
            }

            Map.Entry<List<Double>, Double> bestWeights = calculateRegression(weightSet, pointEntitySets, scoreWeightMap);
            System.out.println(bestWeights);

            if (baseIndex == 7) {
                baseIndex = 0;
            } else {
                baseIndex++;
            }
            baseWeights = bestWeights.getKey();

        }


    }

    private Map.Entry<List<Double>, Double> calculateRegression(Set<List<Double>> weightSet, Set<Set<FullPointEntity>> pointEntitySets, ConcurrentMap<List<Double>, Double> scoreWeightMap) {
        weightSet.stream().gather(Gatherers.mapConcurrent(100, p -> p)).forEach(w ->
        {
            ScoreCalculator.setAveragePointWeight(w.get(0));
            ScoreCalculator.setThreeAveragePointWeight(w.get(1));
            ScoreCalculator.setSimplePredictedPointsWeight(w.get(2));

//            ScoreCalculator.setTrackSimilarityWeight(w.get(0));

            CSVParsing.updateTrackDistances(ScoreCalculator.getTrackMap().values().stream().toList(),ScoreCalculator.getTrackMap(),w);


            Map<String, SquaredErrorValue> squaredErrorValueMap = calculateMeanSquaredErrorValue(pointEntitySets);

            Double meanSquaredError = 0.0;
            int count = 0;
            for (Map.Entry<String, SquaredErrorValue> entry : squaredErrorValueMap.entrySet()) {
                meanSquaredError += entry.getValue().getValue();
                count += entry.getValue().getCount();
            }
            scoreWeightMap.put(w, meanSquaredError / count);
        });

//        scoreWeightMap.entrySet().stream().sorted(Map.Entry.comparingByValue()).limit(25).forEach(System.out::println);
        return scoreWeightMap.entrySet().stream().min(Map.Entry.comparingByValue()).get();
    }

    //TODO:
    //Weekend [Quali score + Race score (+Sprint score)]

    //If race = Singapore
    //then Monaco[total points] *= 1.2
    //then everthing else reduced so weights equal number of races


    //MAYBE:
    //Linear optimization
    //Simplex Algorithm


}
