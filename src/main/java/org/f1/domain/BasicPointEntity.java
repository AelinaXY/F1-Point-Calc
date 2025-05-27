package org.f1.domain;

import java.util.Objects;

public class BasicPointEntity {

    private String name;
    private Double cost;
    private Double averagePoints;
    private Double threeRaceAveragePoints;


    public BasicPointEntity(String name, Double cost, Double averagePoints, Double threeRaceAveragePoints) {
        this.name = name;
        this.cost = cost;
        this.averagePoints = averagePoints;
        this.threeRaceAveragePoints = threeRaceAveragePoints;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public Double getThreeRaceAveragePoints() {
        return threeRaceAveragePoints;
    }
    public void setAveragePoints(Double averagePoints) {
        this.averagePoints = averagePoints;
    }

    public void setThreeRaceAveragePoints(Double threeRaceAveragePoints) {
        this.threeRaceAveragePoints = threeRaceAveragePoints;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BasicPointEntity that = (BasicPointEntity) o;
        return Double.compare(cost, that.cost) == 0 && Double.compare(averagePoints, that.averagePoints) == 0 && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, cost, averagePoints);
    }
}
