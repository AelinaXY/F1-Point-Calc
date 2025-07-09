package org.f1.domain;

import org.f1.calculations.ScoreCalculator;
import org.f1.calculations.ScoreCalculatorInterface;

import java.util.*;

public class ScoreCard {

    private List<FullPointEntity> driverList;
    private List<FullPointEntity> teamList;
    private Double cost = 0.0;
    private Double score = 0.0;

    public ScoreCard() {
        this.driverList = new ArrayList<>();
        this.teamList = new ArrayList<>();
    }

    public ScoreCard(List<FullPointEntity> driverList, List<FullPointEntity> teamList, String race, double costCap, boolean isSprint, ScoreCalculatorInterface scoreCalculator) {
        this.driverList = driverList;
        this.teamList = teamList;
        initialize(race, costCap, isSprint, scoreCalculator);

    }

    private void initialize(String race, double costCap, boolean isSprint, ScoreCalculatorInterface calculator) {
        for (FullPointEntity driver : driverList) {
            cost += driver.getCost();
        }
        for (FullPointEntity team : teamList) {
            cost += team.getCost();
        }

        if (cost <= costCap && cost > costCap - 5) {
            List<Double> driverPointSet = this.driverList.stream().map(d -> calculator.calculateScore(d, race, isSprint)).sorted(Comparator.reverseOrder()).toList();
            score = driverPointSet.stream().reduce(0d, Double::sum);
            score += this.teamList.stream().map(d -> calculator.calculateScore(d, race, isSprint)).reduce(0d, Double::sum);

            score += driverPointSet.getFirst();
        }

    }

    public void intialize(String race, double costCap, boolean isSprint, ScoreCalculatorInterface scoreCalculator) {
        initialize(race, costCap, isSprint, scoreCalculator);
    }

    public void addDriver(FullPointEntity driver) {
        this.driverList.add(driver);
    }

    public void addTeam(FullPointEntity team) {
        this.teamList.add(team);
    }

    public List<FullPointEntity> getDriverList() {
        return driverList;
    }

    public void setDriverList(List<FullPointEntity> driverList) {
        this.driverList = driverList;
    }

    public List<FullPointEntity> getTeamList() {
        return teamList;
    }

    public void setTeamList(List<FullPointEntity> teamList) {
        this.teamList = teamList;
    }

    public Double getCost() {
        return cost;
    }

    public void setCost(Double cost) {
        this.cost = cost;
    }

    public Double getScore() {
        return score;
    }

    @Override
    public String toString() {
        return "ScoreCard{" +
                "cost=" + cost +
                ", score=" + score +
                ", drivers=" + driverList +
                ", teams=" + teamList +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScoreCard scoreCard = (ScoreCard) o;
        return Objects.equals(driverList, scoreCard.driverList) && Objects.equals(teamList, scoreCard.teamList) && Objects.equals(cost, scoreCard.cost) && Objects.equals(score, scoreCard.score);
    }

    @Override
    public int hashCode() {
        return Objects.hash(driverList, teamList, cost, score);
    }

    public DifferenceEntity calculateDifference(ScoreCard scoreCard) {
        DifferenceEntity difference = new DifferenceEntity();
        Set<BasicPointEntity> oldDriverSet = new HashSet<>(driverList);
        Set<BasicPointEntity> oldTeamSet = new HashSet<>(teamList);
        Set<BasicPointEntity> newDriverSet = new HashSet<>(scoreCard.getDriverList());
        Set<BasicPointEntity> newTeamSet = new HashSet<>(scoreCard.getTeamList());

        outDifference(difference, oldDriverSet, scoreCard, true);
        outDifference(difference, oldTeamSet, scoreCard, false);

        inDifference(difference, newDriverSet, this, true);
        inDifference(difference, newTeamSet, this, false);

        difference.setScoreDifference(scoreCard.getScore() - score);

        return difference;
    }

    private void outDifference(DifferenceEntity difference, Set<BasicPointEntity> basicPointEntitySet, ScoreCard scoreCard, Boolean driver) {
        if (driver) {
            basicPointEntitySet.removeAll(scoreCard.getDriverList());
        } else {
            basicPointEntitySet.removeAll(scoreCard.getTeamList());
        }
        difference.addOut(basicPointEntitySet);
        difference.incrementDifference(basicPointEntitySet.size());
    }

    private void inDifference(DifferenceEntity difference, Set<BasicPointEntity> basicPointEntitySet, ScoreCard scoreCard, Boolean driver) {
        if (driver) {
            basicPointEntitySet.removeAll(scoreCard.getDriverList());
        } else {
            basicPointEntitySet.removeAll(scoreCard.getTeamList());
        }
        difference.addIn(basicPointEntitySet);

    }
}
