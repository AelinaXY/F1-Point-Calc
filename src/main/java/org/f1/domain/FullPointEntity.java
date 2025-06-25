package org.f1.domain;

import org.f1.calculations.ScoreCalculator;

import java.util.ArrayList;
import java.util.List;

public class FullPointEntity extends BasicPointEntity {
    private List<Race> raceList;
    private List<String> raceNameList;
    private Double simplePredictedPoints;

    public FullPointEntity(String name, Double cost, List<Race> points) {
        super(name, cost, null, null);

        this.raceList = new ArrayList<>(points);

        List<Double> pointsList = raceList.stream().map(Race::totalPoints).toList();

        Double avgPoint = ScoreCalculator.calcAveragePoints(new ArrayList<>(pointsList));
        setAveragePoints(avgPoint);

        Double threeAvgPoint = ScoreCalculator.calcThreeRaceAverage(new ArrayList<>(pointsList));
        setThreeRaceAveragePoints(threeAvgPoint);

        this.simplePredictedPoints = ScoreCalculator.calcSimplePredictedPoints(new ArrayList<>(pointsList));

        this.raceNameList = raceList.stream().map(Race::name).toList();
    }

    public Double getSimplePredictedPoints() {
        return simplePredictedPoints;
    }

    public List<Race> getRaceList() {
        return raceList;
    }
    public List<String> getRaceNameList() {
        return raceNameList;
    }
}
