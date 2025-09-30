package org.f1.calculations;

import org.f1.domain.BasicPointEntity;
import org.f1.domain.DifferenceEntity;
import org.f1.domain.FullPointEntity;
import org.f1.domain.ScoreCard;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RawDataCalculationV2 extends AbstractCalculation {

    private ScoreCalculatorInterface scoreCalculator;

    private static final Set<ScoreCard> validTeamSet = Collections.synchronizedSet(new HashSet<>());

    private List<FullPointEntity> driverList;
    private List<FullPointEntity> teamList;

    private String raceName;
    private boolean isSprint;
    private double costCap;
    private int racesLeft;
    private double costCapMult;

    public RawDataCalculationV2(Set<FullPointEntity> driverSet, Set<FullPointEntity> teamSet, double costCap, long transferLimit, String raceName, boolean isSprint, ScoreCalculatorInterface calculator, int racesLeft, double costCapMult) {
        super(driverSet, teamSet, costCap, isSprint);
        driverList = new ArrayList<>(driverSet);
        teamList = new ArrayList<>(teamSet);
        this.costCap = costCap;
        this.raceName = raceName;
        this.isSprint = isSprint;

        scoreCalculator = calculator;
        this.racesLeft = racesLeft;
        this.costCapMult = costCapMult;
    }

    public SequencedMap<ScoreCard, DifferenceEntity> calculate(ScoreCard previousScoreCard, boolean consoleLog, int maxSize) {
        IntStream.range(0, driverList.size())
                .parallel()
                .mapToObj(i ->
                        new AbstractMap.SimpleEntry<FullPointEntity, List<FullPointEntity>>(driverList.get(i),
                                new ArrayList<>(driverList.subList(i + 1, driverList.size()))))
                .forEach(driver -> {
                    driverLoop(List.of(driver.getKey()), driver.getValue());
                    if (consoleLog) {
                        System.out.println("Driver " + driver.getKey().getName() + " done");
                    }
                });
        if (consoleLog) {
            System.out.println("Number of valid combinations: " + validTeamSet.size());
        }

        return scoreCardOutput(previousScoreCard, Comparator.comparing(m -> m.getScore() + m.getEffectiveScoreIncrease()), maxSize);
    }

    private SequencedMap<ScoreCard, DifferenceEntity> scoreCardOutput(ScoreCard currentScorecard, Comparator<ScoreCard> comparing, int maxSize) {
        List<ScoreCard> scoreCardList = validTeamSet.stream().sorted(comparing.reversed()).limit(maxSize).toList();

        return scoreCardList.stream()
                .map(sc -> new AbstractMap.SimpleEntry<>(sc, currentScorecard.calculateDifference(sc)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, _) -> a, LinkedHashMap::new));
    }

    private void driverLoop(
            List<FullPointEntity> previousLevelDriverSet, List<FullPointEntity> loopDriverList) {
        if (previousLevelDriverSet.size() == 5) {
            if (!(previousLevelDriverSet.stream().map(BasicPointEntity::getCost).reduce(0d, Double::sum) >= costCap)) {
                teamLoop(new ArrayList<>(), previousLevelDriverSet, teamList);
            }
        } else {
            for (FullPointEntity driver : loopDriverList) {
                List<FullPointEntity> newDriverList = new ArrayList<>(loopDriverList.subList(loopDriverList.indexOf(driver) + 1, loopDriverList.size()));
                if (newDriverList.size() + previousLevelDriverSet.size() >= 4) {
                    List<FullPointEntity> nextLevelDriverSet = new ArrayList<>(previousLevelDriverSet);
                    nextLevelDriverSet.add(driver);
                    driverLoop(nextLevelDriverSet, newDriverList);
                }
            }
        }
    }

    private void teamLoop(
            List<FullPointEntity> previousLevelTeamSet, List<FullPointEntity> driverSet, List<FullPointEntity> loopTeamList) {
        if (previousLevelTeamSet.size() == 2) {
            ScoreCard scoreCard = new ScoreCard(driverSet, previousLevelTeamSet, raceName, costCap, isSprint, scoreCalculator, racesLeft, costCapMult);
            if (scoreCard.getCost() <= costCap && scoreCard.getCost() > costCap - costCap / 10) {
                validTeamSet.add(scoreCard);
            }

        } else {
            for (FullPointEntity team : loopTeamList) {
                List<FullPointEntity> newTeamList = new ArrayList<>(loopTeamList.subList(loopTeamList.indexOf(team) + 1, loopTeamList.size()));
                if (newTeamList.size() + previousLevelTeamSet.size() >= 1) {
                    List<FullPointEntity> nextLevelTeamSet = new ArrayList<>(previousLevelTeamSet);
                    nextLevelTeamSet.add(team);
                    teamLoop(nextLevelTeamSet, driverSet, newTeamList);
                }
            }
        }
    }

    public ScoreCard createPreviousScoreCard(List<String> driverNames, List<String> teamNames, double costCapMult) {
        ScoreCard scoreCard = new ScoreCard();
        getDriverSet().stream().filter(d -> driverNames.contains(d.getName())).forEach(scoreCard::addDriver);
        getTeamSet().stream().filter(t -> teamNames.contains(t.getName())).forEach(scoreCard::addTeam);
        scoreCard.intialize(raceName, costCap, isSprint, scoreCalculator, racesLeft, costCapMult);
        return scoreCard;
    }

    public void resetValues() {
        validTeamSet.clear();
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

    public int getRacesLeft() {
        return racesLeft;
    }

    public void setRacesLeft(int racesLeft) {
        this.racesLeft = racesLeft;
    }

    public double getCostCapMult() {
        return costCapMult;
    }

    public void setCostCapMult(double costCapMult) {
        this.costCapMult = costCapMult;
    }

    public String getRaceName() {
        return raceName;
    }

    public void setRaceName(String raceName) {
        this.raceName = raceName;
    }

    public boolean isSprint() {
        return isSprint;
    }

    public void setSprint(boolean sprint) {
        isSprint = sprint;
    }

    public double getCostCap() {
        return costCap;
    }

    public void setCostCap(double costCap) {
        this.costCap = costCap;
    }
}
