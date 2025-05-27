package org.f1.domain;

import java.util.ArrayList;
import java.util.List;

public class FullPointEntity extends BasicPointEntity {
    List<Double> points;

    public FullPointEntity(String name, Double cost, List<Double> points) {
        super(name, cost, null, null);

        this.points = new ArrayList<>(points);

        setAveragePoints(calcAveragePoints(this.points));
        setThreeRaceAveragePoints(calcThreeRaceAverage(this.points));
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
