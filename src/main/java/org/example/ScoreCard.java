package org.example;

import java.util.Comparator;
import java.util.Objects;
import java.util.Set;

public class ScoreCard {

    private Set<PointEntity> driverSet;
    private Set<PointEntity> teamSet;
    private Double cost;
    private Double averagePoints;

    public ScoreCard() {
    }

    public ScoreCard(Set<PointEntity> driverSet, Set<PointEntity> teamSet) {
        this.driverSet = driverSet;
        this.teamSet = teamSet;
        cost = driverSet.stream().map(PointEntity::getCost).reduce(0d, Double::sum);
        cost += teamSet.stream().map(PointEntity::getCost).reduce(0d, Double::sum);
        averagePoints = driverSet.stream().map(PointEntity::getAveragePoints).reduce(0d, Double::sum);
        averagePoints += teamSet.stream().map(PointEntity::getAveragePoints).reduce(0d, Double::sum);

        averagePoints += driverSet.stream().sorted(Comparator.comparing(PointEntity::getAveragePoints).reversed()).limit(1).map(PointEntity::getAveragePoints).findFirst().orElse(null);
    }

    public Set<PointEntity> getDriverSet() {
        return driverSet;
    }

    public void setDriverSet(Set<PointEntity> driverSet) {
        this.driverSet = driverSet;
    }

    public Set<PointEntity> getTeamSet() {
        return teamSet;
    }

    public void setTeamSet(Set<PointEntity> teamSet) {
        this.teamSet = teamSet;
    }

    public Double getCost() {
        return cost;
    }

    public void setCost(Double cost) {
        this.cost = cost;
    }

    public Double getAveragePoints() {
        return averagePoints;
    }

    public void setAveragePoints(Double averagePoints) {
        this.averagePoints = averagePoints;
    }

    @Override
    public String toString() {
        return "ScoreCard{" +
                "cost=" + cost +
                ", averagePoints=" + averagePoints +
                ", drivers=" + driverSet +
                ", teams=" + teamSet +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScoreCard scoreCard = (ScoreCard) o;
        return Objects.equals(driverSet, scoreCard.driverSet) && Objects.equals(teamSet, scoreCard.teamSet) && Objects.equals(cost, scoreCard.cost) && Objects.equals(averagePoints, scoreCard.averagePoints);
    }

    @Override
    public int hashCode() {
        return Objects.hash(driverSet, teamSet, cost, averagePoints);
    }
}
