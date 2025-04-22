package org.f1;

import org.f1.enums.Drivers;
import org.f1.enums.Teams;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class ScoreCard {

    private Set<PointEntity> driverSet;
    private Set<PointEntity> teamSet;
    private Double cost;
    private Double averagePoints;
    private Double threeRaceAveragePoints;

    public ScoreCard() {
    }

    public ScoreCard(Set<Drivers> driverSet, Set<Teams> teamSet) {
        this.driverSet = driverSet.stream().map(Drivers::getPointEntity).collect(Collectors.toSet());
        this.teamSet = teamSet.stream().map(Teams::getPointEntity).collect(Collectors.toSet());
        cost = this.driverSet.stream().map(PointEntity::getCost).reduce(0d, Double::sum);
        cost += this.teamSet.stream().map(PointEntity::getCost).reduce(0d, Double::sum);
        averagePoints = this.driverSet.stream().map(PointEntity::getAveragePoints).reduce(0d, Double::sum);
        averagePoints += this.teamSet.stream().map(PointEntity::getAveragePoints).reduce(0d, Double::sum);

        threeRaceAveragePoints = this.driverSet.stream().map(PointEntity::getThreeRaceAveragePoints).reduce(0d, Double::sum);
        threeRaceAveragePoints += this.teamSet.stream().map(PointEntity::getThreeRaceAveragePoints).reduce(0d, Double::sum);

        averagePoints += this.driverSet.stream().sorted(Comparator.comparing(PointEntity::getAveragePoints).reversed()).limit(1).map(PointEntity::getAveragePoints).findFirst().orElse(null);
        threeRaceAveragePoints += this.driverSet.stream().sorted(Comparator.comparing(PointEntity::getAveragePoints).reversed()).limit(1).map(PointEntity::getThreeRaceAveragePoints).findFirst().orElse(null);

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

    public Double getThreeRaceAveragePoints() {
        return threeRaceAveragePoints;
    }


    public void setAveragePoints(Double averagePoints) {
        this.averagePoints = averagePoints;
    }

    @Override
    public String toString() {
        return "ScoreCard{" +
                "cost=" + cost +
                ", averagePoints=" + averagePoints +
                ", threeRacePoints=" + threeRaceAveragePoints +
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

    public DifferenceEntity calculateDifference(ScoreCard scoreCard) {
        DifferenceEntity difference = new DifferenceEntity();
        Set<PointEntity> oldDriverSet = new HashSet<>(driverSet);
        Set<PointEntity> oldTeamSet = new HashSet<>(teamSet);
        Set<PointEntity> newDriverSet = new HashSet<>(scoreCard.getDriverSet());
        Set<PointEntity> newTeamSet = new HashSet<>(scoreCard.getTeamSet());

        outDifference(difference,oldDriverSet,scoreCard,true);
        outDifference(difference,oldTeamSet,scoreCard,false);

        inDifference(difference,newDriverSet,this,true);
        inDifference(difference,newTeamSet,this,false);

        difference.setPointDifference(scoreCard.averagePoints-averagePoints);
        difference.setThreeRacePointDifference(scoreCard.threeRaceAveragePoints-threeRaceAveragePoints);


        return difference;
    }

    private void outDifference(DifferenceEntity difference, Set<PointEntity> pointEntitySet, ScoreCard scoreCard, Boolean driver) {
        if(driver) {
            pointEntitySet.removeAll(scoreCard.getDriverSet());
        }
        else{
            pointEntitySet.removeAll(scoreCard.getTeamSet());
        }
        difference.addOut(pointEntitySet);
        difference.incrementDifference(pointEntitySet.size());
    }

    private void inDifference(DifferenceEntity difference, Set<PointEntity> pointEntitySet, ScoreCard scoreCard, Boolean driver) {
        if(driver) {
            pointEntitySet.removeAll(scoreCard.getDriverSet());
        }
        else{
            pointEntitySet.removeAll(scoreCard.getTeamSet());
        }
        difference.addIn(pointEntitySet);

    }
}
