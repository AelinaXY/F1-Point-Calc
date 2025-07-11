package org.f1.domain;

import org.f1.calculations.ScoreCalculator;
import org.f1.enums.EntityType;

import java.util.ArrayList;
import java.util.List;

public class FullPointEntity extends BasicPointEntity {
    private List<Race> raceList;
    private List<String> raceNameList;
    private Double simplePredictedPoints;
    private EntityType entityType;

    public FullPointEntity(String name, Double cost, List<Race> points, EntityType entityType) {
        super(name, cost, null, null);

        this.raceList = new ArrayList<>(points);

        List<Double> pointsList = raceList.stream().map(Race::totalPoints).toList();

        Double avgPoint = ScoreCalculator.calcAveragePoints(new ArrayList<>(pointsList));
        setAveragePoints(avgPoint);

        Double threeAvgPoint = ScoreCalculator.calcThreeRaceAverage(new ArrayList<>(pointsList));
        setThreeRaceAveragePoints(threeAvgPoint);

        this.simplePredictedPoints = ScoreCalculator.calcSimplePredictedPoints(new ArrayList<>(pointsList));

        this.raceNameList = raceList.stream().map(Race::name).toList();

        this.entityType = entityType;
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

    public EntityType getEntityType() {
        return entityType;
    }

    public boolean isDriver() {
        return entityType == EntityType.DRIVER;
    }

    public boolean isTeam() {
        return entityType == EntityType.TEAM;
    }
}
