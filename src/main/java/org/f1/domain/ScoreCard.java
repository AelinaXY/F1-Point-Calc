package org.f1.domain;

import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.f1.calculations.CostCalculator;
import org.f1.calculations.ScoreCalculatorInterface;

import java.util.*;

public class ScoreCard {
    private List<FullPointEntity> driverList;
    private List<FullPointEntity> teamList;
    private Double cost = 0.0;
    private Double score = 0.0;
    private Double costChange = 0.0;
    private Double effectiveScoreIncrease = 0.0;

    public ScoreCard() {
        this.driverList = new ArrayList<>();
        this.teamList = new ArrayList<>();
    }

    public ScoreCard(List<FullPointEntity> driverList, List<FullPointEntity> teamList, String race, double costCap, boolean isSprint, ScoreCalculatorInterface scoreCalculator, int racesLeft, double costCapMult) {
        this.driverList = driverList;
        this.teamList = teamList;
        initialize(race, costCap, isSprint, scoreCalculator, racesLeft, costCapMult);

    }

    private void initialize(String race, double costCap, boolean isSprint, ScoreCalculatorInterface calculator, int racesLeft, double costCapMult) {
        cost = 0.0;
        score = 0.0;
        costChange = 0.0;
        effectiveScoreIncrease = 0.0;

        for (FullPointEntity driver : driverList) {
            cost += driver.getCost();
        }
        for (FullPointEntity team : teamList) {
            cost += team.getCost();
        }


        if (cost <= costCap && cost > costCap - costCap / 10) {
            List<Double> driverScoreList = new ArrayList<>();
            List<Double> teamScoreList = new ArrayList<>();

            for (FullPointEntity driver : driverList) {
                Double driverScore = calculator.calculateScore(driver, race, isSprint);
                driverScoreList.add(driverScore);
                costChange += CostCalculator.calculateCostChange(driver, race, driverScore);
            }
            for (FullPointEntity team : teamList) {
                Double teamScore = calculator.calculateScore(team, race, isSprint);
                teamScoreList.add(teamScore);
                costChange += CostCalculator.calculateCostChange(team, race, teamScore);
            }

            driverScoreList = driverScoreList.stream().sorted(Comparator.reverseOrder()).toList();
            score = driverScoreList.stream().reduce(0d, Double::sum);
            score += teamScoreList.stream().reduce(0d, Double::sum);

            score += driverScoreList.getFirst();
            effectiveScoreIncrease += costChange * costCapMult * racesLeft;
        }

    }

    public void intialize(String race, double costCap, boolean isSprint, ScoreCalculatorInterface scoreCalculator, int racesLeft, double costCapMult) {
        initialize(race, costCap, isSprint, scoreCalculator, racesLeft, costCapMult);
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

    public Double getCostChange() {
        return costChange;
    }

    @Override
    public String toString() {
        return "ScoreCard{" +
                "cost=" + cost +
                ", score=" + score +
                ", costChange=" + costChange +
                ", effectiveScoreIncrease=" + effectiveScoreIncrease +
                ", drivers=" + driverList +
                ", teams=" + teamList +
                '}';
    }

    public JSONObject toJSON() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("cost", Math.round(cost * 100.0) / 100.0);
        jsonObject.put("score", Math.round(score * 100.0) / 100.0);
        jsonObject.put("costChange", Math.round(costChange * 100.0) / 100.0);
        jsonObject.put("effectiveScoreIncrease", Math.round(effectiveScoreIncrease * 100.0) / 100.0);
        jsonObject.put("drivers", driverList.stream().map(BasicPointEntity::toString).toList());
        jsonObject.put("teams", teamList.stream().map(BasicPointEntity::toString).toList());
        return jsonObject;
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
        difference.setCostChangeDifference(scoreCard.getCostChange() - costChange);

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

    public Double getEffectiveScoreIncrease() {
        return effectiveScoreIncrease;
    }

    public void setEffectiveScoreIncrease(Double effectiveScoreIncrease) {
        this.effectiveScoreIncrease = effectiveScoreIncrease;
    }
}
