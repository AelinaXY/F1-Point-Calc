package org.f1.calculations;

import org.f1.domain.FullPointEntity;
import org.f1.domain.Race;
import org.f1.domain.RegressionResolution;
import org.f1.domain.SquaredErrorValue;
import org.f1.parsing.CSVParsing;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import java.util.stream.Gatherers;

import static org.f1.domain.EntityType.*;

public class RegressionDataCalculation extends AbstractCalculation {

    private ScoreCalculator scoreCalculatorV1 = new ScoreCalculator();
    private ScoreCalculatorV2 scoreCalculatorV2 = new ScoreCalculatorV2();


    public RegressionDataCalculation(Set<FullPointEntity> driverSet, Set<FullPointEntity> teamSet) {
        super(driverSet, teamSet);

    }


    //TEST sMAPE and MSE_log
    public Double calculateErrorValue(FullPointEntity entity, String race, ScoreCalculatorInterface calculator) {
        List<Race> raceList = entity.getRaceList();
        Optional<Race> actualRace = raceList.stream().filter(race1 -> race1.name().equals(race)).findFirst();

        if (actualRace.isPresent()) {
            Double expectedScore = calculator.calculateScore(entity, race, actualRace.get().isSprint());
            Double actualScore = actualRace.get().totalPoints();
            return expectedScore - actualScore;
        }
        return Double.NaN;
    }

    public Map<String, SquaredErrorValue> calculateMeanSquaredErrorValue(Set<Set<FullPointEntity>> entitySets, ScoreCalculatorInterface calculator) {
        Set<FullPointEntity> entitySet = entitySets.stream().flatMap(Set::stream).collect(Collectors.toSet());
        Map<String, SquaredErrorValue> meanSquaredErrorMap = new HashMap<>();

        for (FullPointEntity entity : entitySet) {
            List<Race> raceList = new ArrayList<>(entity.getRaceList());
            raceList.removeFirst();
            for (Race race : raceList) {
                Double errorValue = calculateErrorValue(entity, race.name(), calculator);
                meanSquaredErrorMap.compute(race.name(), (_, v) -> v == null ? new SquaredErrorValue(errorValue) : v.increment(errorValue));
            }
        }
        return meanSquaredErrorMap;
    }

    private <T> Map.Entry<T, Double> calculateSMEacrossSet(Set<Set<FullPointEntity>> pointEntitySets, ScoreCalculatorInterface calculator, T label) {
        Map<String, SquaredErrorValue> squaredErrorValueMap = calculateMeanSquaredErrorValue(pointEntitySets, calculator);

        Double meanSquaredError = 0.0;
        int count = 0;
        for (Map.Entry<String, SquaredErrorValue> entry : squaredErrorValueMap.entrySet()) {
            meanSquaredError += entry.getValue().getValue();
            count += entry.getValue().getCount();
        }
        return new AbstractMap.SimpleEntry<>(label, meanSquaredError / count);
    }

    public Map<String, Double> compareScoreCalculators() {
        HashMap<String, Double> returnMap = new HashMap<>();

        Set<FullPointEntity> drivers2024 = CSVParsing.parseFullPointEntities("Drivers_Full_2024.csv", DRIVER);
        Set<FullPointEntity> teams2024 = CSVParsing.parseFullPointEntities("Teams_Full_2024.csv", TEAM);

        Set<Set<FullPointEntity>> pointEntitySets = Set.of(getDriverSet(), getTeamSet(), drivers2024, teams2024);

        Map.Entry<String, Double> bestWeightsV1 = calculateSMEacrossSet(pointEntitySets, scoreCalculatorV1, "V1");
        returnMap.put(bestWeightsV1.getKey(), bestWeightsV1.getValue());

        Map.Entry<String, Double> bestWeightsV2 = calculateSMEacrossSet(pointEntitySets, scoreCalculatorV2, "V2");
        returnMap.put(bestWeightsV2.getKey(), bestWeightsV2.getValue());

        return returnMap;
    }

    public List<Double> regressionCalculation(int iterations) {
        Set<FullPointEntity> drivers2024 = CSVParsing.parseFullPointEntities("Drivers_Full_2024.csv", DRIVER);
        Set<FullPointEntity> teams2024 = CSVParsing.parseFullPointEntities("Teams_Full_2024.csv", TEAM);

        Set<Set<FullPointEntity>> pointEntitySets = Set.of(getDriverSet(), getTeamSet(), drivers2024, teams2024);

        List<Double> baseWeights = new ArrayList<>(List.of(0.43d, 0.49d, 0.1d, 0.134d, 1.15d));
        int baseIndex = 0;
        RegressionResolution resolution = new RegressionResolution(2d, 0.01d);

        for (int i = 0; i < iterations * baseWeights.size(); i++) {
            Set<List<Double>> weightSet = new HashSet<>();
            ConcurrentMap<List<Double>, Double> scoreWeightMap = new ConcurrentHashMap<>();

            double valueToIterate = baseWeights.get(baseIndex);
            for (double j = valueToIterate - resolution.getEdgeBounds(); j <= valueToIterate + resolution.getEdgeBounds(); j += resolution.getInterval()) {
                List<Double> newWeights = new ArrayList<>(baseWeights);
                newWeights.set(baseIndex, j);
                weightSet.add(newWeights);
            }

            Map.Entry<List<Double>, Double> bestWeights = calculateRegression(weightSet, pointEntitySets, scoreWeightMap, scoreCalculatorV1);

            if (baseIndex == 4) {
                baseIndex = 0;
            } else {
                baseIndex++;
            }

            if (baseIndex == 0 && baseWeights.equals(bestWeights.getKey())) {
                resolution.lowerResolution();
            }
            baseWeights = bestWeights.getKey();

        }
        return baseWeights;


    }

    private Map.Entry<List<Double>, Double> calculateRegression(Set<List<Double>> weightSet, Set<Set<FullPointEntity>> pointEntitySets, ConcurrentMap<List<Double>, Double> scoreWeightMap, ScoreCalculatorInterface calculator) {
        weightSet.stream().gather(Gatherers.mapConcurrent(100, p -> p)).forEach(w ->
        {
            ScoreCalculator.setAveragePointWeight(w.get(0));
            ScoreCalculator.setThreeAveragePointWeight(w.get(1));
            ScoreCalculator.setSimplePredictedPointsWeight(w.get(2));
            ScoreCalculator.setTrackSimilarityWeight(w.get(3));
            ScoreCalculator.setSprintWeight(w.get(4));
            Map.Entry<List<Double>, Double> entry = calculateSMEacrossSet(pointEntitySets, calculator, w);
            scoreWeightMap.put(entry.getKey(), entry.getValue());
        });

        return scoreWeightMap.entrySet().stream().min(Map.Entry.comparingByValue()).get();
    }
}
