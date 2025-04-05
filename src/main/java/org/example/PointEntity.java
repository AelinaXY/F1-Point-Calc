package org.example;

public class PointEntity {

    private String name;
    private double cost;
    private double averagePoints;

    public PointEntity(String name, double cost, double averagePoints) {
        this.name = name;
        this.cost = cost;
        this.averagePoints = averagePoints;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public double getAveragePoints() {
        return averagePoints;
    }

    public void setAveragePoints(double averagePoints) {
        this.averagePoints = averagePoints;
    }

    @Override
    public String toString() {
        return name;
    }
}
