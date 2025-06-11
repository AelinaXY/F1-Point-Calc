package org.f1.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FullPointEntity extends BasicPointEntity {
    private List<Race> raceList;
    private Double updatedAveragePoints;
    private Double updatedThreeRaceAveragePoints;
    private Double simplePredictedPoints;

    public FullPointEntity(String name, Double cost, List<Race> points) {
        super(name, cost, null, null);

        this.raceList = new ArrayList<>(points);

        List<Double> pointsList = raceList.stream().map(Race::totalPoints).toList();

        Double avgPoint = calcAveragePoints(new ArrayList<>(pointsList));
        setAveragePoints(avgPoint);
        this.updatedAveragePoints = avgPoint;

        Double threeAvgPoint = calcThreeRaceAverage(new ArrayList<>(pointsList));
        setThreeRaceAveragePoints(threeAvgPoint);
        this.updatedThreeRaceAveragePoints = threeAvgPoint;

        this.simplePredictedPoints = this.calcSimplePredictedPoints(new ArrayList<>(pointsList));
    }

    public void calculateUpdatedPoints(String raceName) {
        List<Race> currentRaces = new ArrayList<>();

        for (Race race : raceList) {
            if (race.name().equals(raceName)) {
                break;
            }
            currentRaces.add(race);
        }

        List<Double> points = currentRaces.stream().map(Race::totalPoints).toList();
        this.updatedAveragePoints = calcAveragePoints(new ArrayList<>(points));
        this.updatedThreeRaceAveragePoints = calcThreeRaceAverage(new ArrayList<>(points));
        this.simplePredictedPoints = calcSimplePredictedPoints(new ArrayList<>(points));
    }

    private Double calcSimplePredictedPoints(List<Double> points) {
        if (points.size() > 2) {
            Double gradient = 0.0;
            for (int i = 1; i < points.size(); i++) {
                gradient += ((points.get(i - 1) - points.get(i))) / points.size()-1;
            }
            return points.getLast() + gradient;
        }
        return null;
    }

    private Double calcAveragePoints(List<Double> points) {
        return points.stream().mapToDouble(Double::doubleValue).average().getAsDouble();
    }

    private Double calcThreeRaceAverage(List<Double> points) {
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

    public Double getUpdatedThreeRaceAveragePoints() {
        return updatedThreeRaceAveragePoints;
    }

    public Double getUpdatedAveragePoints() {
        return updatedAveragePoints;
    }

    public Double getSimplePredictedPoints() {
        return simplePredictedPoints;
    }

    public List<Race> getRaceList() {
        return raceList;
    }
}
