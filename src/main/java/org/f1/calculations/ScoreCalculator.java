package org.f1.calculations;

import org.f1.domain.FullPointEntity;
import org.f1.domain.Race;
import org.f1.domain.Track;
import org.f1.parsing.CSVParsing;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ScoreCalculator {

    private static Double averagePointWeight = 0.58;
    private static Double threeAveragePointWeight = 0.36;
    private static Double simplePredictedPointsWeight = 0.06;
    private static Double trackSimilarityWeight = 0.13;

    private static final Map<String, Track> TRACK_MAP = CSVParsing.parseTracks("Tracks_Normalised.csv");


    public static Double calculateScore(FullPointEntity fullPointEntity, String race) {
        double runningTotal = 0.0;

        ScoreCalculatorHelper scores = calculateUpdatedPoints(fullPointEntity, race);

        runningTotal += scores.averagePoints * averagePointWeight;

        runningTotal += scores.threeAveragePoints * threeAveragePointWeight;

        if (scores.simplePredictedPoints != null) {
            runningTotal += scores.simplePredictedPoints * simplePredictedPointsWeight;
        } else {
            runningTotal += scores.averagePoints * simplePredictedPointsWeight * (averagePointWeight / (averagePointWeight + threeAveragePointWeight));

            runningTotal += scores.threeAveragePoints * simplePredictedPointsWeight * (threeAveragePointWeight / (averagePointWeight + threeAveragePointWeight));
        }

        return runningTotal;
    }

    private static ScoreCalculatorHelper calculateUpdatedPoints(FullPointEntity fullPointEntity, String raceName) {
        List<String> raceNameList = fullPointEntity.getRaceNameList();

//        if (!raceNameList.contains(raceName)) {
//            return new ScoreCalculatorHelper(fullPointEntity.getAveragePoints(),
//                    fullPointEntity.getThreeRaceAveragePoints(),
//                    fullPointEntity.getSimplePredictedPoints());
//        }
        List<Race> currentRaces = new ArrayList<>();

        for (Race race : fullPointEntity.getRaceList()) {
            if (race.name().equals(raceName)) {
                break;
            }
            currentRaces.add(race);
        }

        Map<String, Double> trackSimilarities = TRACK_MAP.get(raceName).getDistanceToOtherTrack();

        List<Double> points = currentRaces.stream().map(r ->
                {
                    String currentRaceName = r.name();
                    double similarity = trackSimilarities.get(currentRaceName);

                    double runningTotal = r.totalPoints();

                    runningTotal += r.totalPoints() * similarity * trackSimilarityWeight;
                    return runningTotal;
                }
        ).toList();

        return
                new ScoreCalculatorHelper(
                        calcAveragePoints(new ArrayList<>(points)),
                        calcThreeRaceAverage(new ArrayList<>(points)),
                        calcSimplePredictedPoints(new ArrayList<>(points)));
    }

    public static Double calcSimplePredictedPoints(List<Double> points) {
        if (points.size() > 2) {
            Double gradient = 0.0;
            for (int i = 1; i < points.size(); i++) {
                gradient += ((points.get(i - 1) - points.get(i))) / points.size() - 1;
            }
            return points.getLast() + gradient;
        }
        return null;
    }

    public static Double calcAveragePoints(List<Double> points) {
        return points.stream().mapToDouble(Double::doubleValue).average().getAsDouble();
    }

    public static Double calcThreeRaceAverage(List<Double> points) {
        Double lowest = 1000.0;
        switch (points.size()) {
            case 0:
                return null;
            case 1:
                return points.getFirst();
            case 2, 3:
                removeLowest(points, lowest);
                return calcAveragePoints(points);
            default:
                points = new ArrayList<>(points.subList(points.size() - 4, points.size()));
                removeLowest(points, lowest);
                return calcAveragePoints(points);
        }
    }

    private static void removeLowest(List<Double> points, Double lowest) {
        for (Double point : points) {
            if (point < lowest) lowest = point;
        }
        points.remove(lowest);
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

    public static void setTrackSimilarityWeight(Double trackSimilarityWeight) {
        ScoreCalculator.trackSimilarityWeight = trackSimilarityWeight;
    }

    public record ScoreCalculatorHelper(Double averagePoints, Double threeAveragePoints, Double simplePredictedPoints) {
    }
}
