package org.f1.domain;

import lombok.Getter;
import org.f1.calculations.ScoreCalculator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
public class FullPointEntity extends BasicPointEntity {
    private final List<Race> raceList;
    private final List<String> raceNameList;
    private final Double simplePredictedPoints;
    private final EntityType entityType;
    private Double baseCost;
    private final int year;

    public FullPointEntity(String name, Double cost, List<Race> points, EntityType entityType, Double baseCost, int year) {
        super(name, cost, null, null);

        this.raceList = new ArrayList<>(points);
        this.baseCost = baseCost;
        this.year = year;

        List<Double> pointsList = raceList.stream().map(Race::totalPoints).toList();

        Double avgPoint = ScoreCalculator.calcAveragePoints(new ArrayList<>(pointsList));
        setAveragePoints(avgPoint);

        Double threeAvgPoint = ScoreCalculator.calcThreeRaceAverage(new ArrayList<>(pointsList));
        setThreeRaceAveragePoints(threeAvgPoint);

        this.simplePredictedPoints = ScoreCalculator.calcSimplePredictedPoints(new ArrayList<>(pointsList));

        this.raceNameList = raceList.stream().map(Race::name).toList();

        this.entityType = entityType;
    }

    public boolean isDriver() {
        return entityType == EntityType.DRIVER;
    }

    public boolean isTeam() {
        return entityType == EntityType.TEAM;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        FullPointEntity that = (FullPointEntity) o;
        return Objects.equals(raceList, that.raceList) && Objects.equals(raceNameList, that.raceNameList) && Objects.equals(simplePredictedPoints, that.simplePredictedPoints) && entityType == that.entityType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), raceList, raceNameList, simplePredictedPoints, entityType);
    }

    public void setBaseCost(Double baseCost) {
        this.baseCost = baseCost;
    }

}
