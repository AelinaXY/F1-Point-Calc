package org.f1.domain;

public class Race {

    private String name;
    private int raceNumber;
    private Double sprintPoints;
    private Double qualiPoints;
    private Double racePoints;

    public Race(String name, int raceNumber, Double sprintPoints, Double qualiPoints, Double racePoints) {
        this.name = name;
        this.raceNumber = raceNumber;
        this.sprintPoints = sprintPoints;
        this.qualiPoints = qualiPoints;
        this.racePoints = racePoints;
    }

    public boolean isSprint(){
        return sprintPoints != null;
    }



}
