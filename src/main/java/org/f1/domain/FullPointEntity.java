package org.f1.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FullPointEntity extends BasicPointEntity {
    private List<Race> racePoints;

    public FullPointEntity(String name, Double cost, List<Race> points) {
        super(name, cost, null, null);

        this.racePoints = new ArrayList<>(points);

        setAveragePoints(calcAveragePoints(this.racePoints.stream().map(Race::totalPoints).collect(Collectors.toList())));
        setThreeRaceAveragePoints(calcThreeRaceAverage(this.racePoints.stream().map(Race::totalPoints).collect(Collectors.toList())));
    }

    private Double calcAveragePoints(List<Double> points) {
        return points.stream().mapToDouble(Double::doubleValue).average().getAsDouble();
    }

    private Double calcThreeRaceAverage(List<Double> points) {
        Double lowest = 1000.0;
        switch(points.size()) {
            case 0: return null;
            case 1: return points.getFirst();
            case 2, 3:
                removeLowest(points, lowest);
                return calcAveragePoints(points);
            default:
                points = new ArrayList<>(points.subList(points.size()-4, points.size()));
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
}
