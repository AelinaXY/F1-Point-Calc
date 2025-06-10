package org.f1.domain;

import org.f1.calculations.ScoreCalculator;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class ScoreCard {

    private Set<FullPointEntity> driverSet;
    private Set<FullPointEntity> teamSet;
    private Double cost;
    private Double averagePoints;
    private Double threeRaceAveragePoints;
    private Double score;

    public ScoreCard() {
        this.driverSet = new HashSet<>();
        this.teamSet = new HashSet<>();
    }

    public ScoreCard(Set<FullPointEntity> driverSet, Set<FullPointEntity> teamSet) {
        this.driverSet = driverSet;
        this.teamSet = teamSet;
        initialize();

    }

    private void initialize() {
        cost = this.driverSet.stream().map(BasicPointEntity::getCost).reduce(0d, Double::sum);
        cost += this.teamSet.stream().map(BasicPointEntity::getCost).reduce(0d, Double::sum);
        averagePoints = this.driverSet.stream().map(BasicPointEntity::getAveragePoints).reduce(0d, Double::sum);
        averagePoints += this.teamSet.stream().map(BasicPointEntity::getAveragePoints).reduce(0d, Double::sum);

        threeRaceAveragePoints = this.driverSet.stream().map(BasicPointEntity::getThreeRaceAveragePoints).reduce(0d, Double::sum);
        threeRaceAveragePoints += this.teamSet.stream().map(BasicPointEntity::getThreeRaceAveragePoints).reduce(0d, Double::sum);

        score = this.driverSet.stream().map(ScoreCalculator::calculateScore).reduce(0d, Double::sum);
        score += this.teamSet.stream().map(ScoreCalculator::calculateScore).reduce(0d, Double::sum);

        averagePoints += this.driverSet.stream().sorted(Comparator.comparing(BasicPointEntity::getAveragePoints).reversed()).limit(1).map(BasicPointEntity::getAveragePoints).findFirst().orElse(null);
        threeRaceAveragePoints += this.driverSet.stream().sorted(Comparator.comparing(BasicPointEntity::getThreeRaceAveragePoints).reversed()).limit(1).map(BasicPointEntity::getThreeRaceAveragePoints).findFirst().orElse(null);
        score += this.driverSet.stream().sorted(Comparator.comparing(ScoreCalculator::calculateScore).reversed()).limit(1).map(ScoreCalculator::calculateScore).findFirst().orElse(null);

    }

    public void intialize() {
        initialize();
    }

    public void addDriver(FullPointEntity driver) {
        this.driverSet.add(driver);
    }

    public void addTeam(FullPointEntity team) {
        this.teamSet.add(team);
    }

    public Set<FullPointEntity> getDriverSet() {
        return driverSet;
    }

    public void setDriverSet(Set<FullPointEntity> driverSet) {
        this.driverSet = driverSet;
    }

    public Set<FullPointEntity> getTeamSet() {
        return teamSet;
    }

    public void setTeamSet(Set<FullPointEntity> teamSet) {
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

    public Double getScore() {
        return score;
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
        Set<BasicPointEntity> oldDriverSet = new HashSet<>(driverSet);
        Set<BasicPointEntity> oldTeamSet = new HashSet<>(teamSet);
        Set<BasicPointEntity> newDriverSet = new HashSet<>(scoreCard.getDriverSet());
        Set<BasicPointEntity> newTeamSet = new HashSet<>(scoreCard.getTeamSet());

        outDifference(difference,oldDriverSet,scoreCard,true);
        outDifference(difference,oldTeamSet,scoreCard,false);

        inDifference(difference,newDriverSet,this,true);
        inDifference(difference,newTeamSet,this,false);

        difference.setPointDifference(scoreCard.averagePoints-averagePoints);
        difference.setThreeRacePointDifference(scoreCard.threeRaceAveragePoints-threeRaceAveragePoints);


        return difference;
    }

    private void outDifference(DifferenceEntity difference, Set<BasicPointEntity> basicPointEntitySet, ScoreCard scoreCard, Boolean driver) {
        if(driver) {
            basicPointEntitySet.removeAll(scoreCard.getDriverSet());
        }
        else{
            basicPointEntitySet.removeAll(scoreCard.getTeamSet());
        }
        difference.addOut(basicPointEntitySet);
        difference.incrementDifference(basicPointEntitySet.size());
    }

    private void inDifference(DifferenceEntity difference, Set<BasicPointEntity> basicPointEntitySet, ScoreCard scoreCard, Boolean driver) {
        if(driver) {
            basicPointEntitySet.removeAll(scoreCard.getDriverSet());
        }
        else{
            basicPointEntitySet.removeAll(scoreCard.getTeamSet());
        }
        difference.addIn(basicPointEntitySet);

    }
}
