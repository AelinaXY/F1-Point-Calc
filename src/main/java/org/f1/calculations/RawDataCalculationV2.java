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

    private int racesLeft;

    public RawDataCalculationV2(Set<FullPointEntity> driverSet, Set<FullPointEntity> teamSet, double costCap, long transferLimit, String raceName, boolean isSprint, ScoreCalculatorInterface calculator, int racesLeft) {
        super(driverSet, teamSet, costCap, transferLimit, raceName, isSprint);
        driverList = new ArrayList<>(driverSet);
        teamList = new ArrayList<>(teamSet);
        scoreCalculator = calculator;
        this.racesLeft = racesLeft;
    }

    public Map<ScoreCard, DifferenceEntity> calculate(ScoreCard previousScoreCard, boolean consoleLog) {
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

        System.out.println("Number of valid combinations: " + validTeamSet.size());

        return scoreCardOutput(previousScoreCard, Comparator.comparing(m -> m.getScore() + m.getEffectiveScoreIncrease()));
    }

    private Map<ScoreCard, DifferenceEntity> scoreCardOutput(ScoreCard currentScorecard, Comparator<ScoreCard> comparing) {
        List<ScoreCard> scoreCardList = validTeamSet.stream().sorted(comparing.reversed()).limit(30).toList();

        return scoreCardList.stream()
                .map(sc -> new AbstractMap.SimpleEntry<>(sc, currentScorecard.calculateDifference(sc)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, _) -> a, LinkedHashMap::new));
    }

    private void driverLoop(
            List<FullPointEntity> previousLevelDriverSet, List<FullPointEntity> loopDriverList) {
        if (previousLevelDriverSet.size() == 5) {
            if (!(previousLevelDriverSet.stream().map(BasicPointEntity::getCost).reduce(0d, Double::sum) >= getCostCap())) {
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
            ScoreCard scoreCard = new ScoreCard(driverSet, previousLevelTeamSet, getRaceName(), getCostCap(), isSprint(), scoreCalculator, racesLeft);
            if (scoreCard.getCost() <= getCostCap() && scoreCard.getCost() > getCostCap() - 5) {
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

    public ScoreCard createPreviousScoreCard(List<String> driverNames, List<String> teamNames) {
        ScoreCard scoreCard = new ScoreCard();
        getDriverSet().stream().filter(d -> driverNames.contains(d.getName())).forEach(scoreCard::addDriver);
        getTeamSet().stream().filter(t -> teamNames.contains(t.getName())).forEach(scoreCard::addTeam);
        scoreCard.intialize(getRaceName(), getCostCap(), isSprint(), scoreCalculator, racesLeft);
        return scoreCard;
    }
}
