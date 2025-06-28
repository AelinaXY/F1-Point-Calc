package org.f1.calculations;

import org.f1.domain.BasicPointEntity;
import org.f1.domain.DifferenceEntity;
import org.f1.domain.FullPointEntity;
import org.f1.domain.ScoreCard;

import java.util.*;
import java.util.stream.IntStream;

public class RawDataCalculationV2 extends AbstractCalculation {

    private static final Set<ScoreCard> validTeamSet = Collections.synchronizedSet(new HashSet<>());

    private List<FullPointEntity> driverList;
    private List<FullPointEntity> teamList;

    public RawDataCalculationV2(Set<FullPointEntity> driverSet, Set<FullPointEntity> teamSet, double costCap, long transferLimit, String raceName) {
        super(driverSet, teamSet, costCap, transferLimit, raceName);
        driverList = new ArrayList<>(driverSet);
        teamList = new ArrayList<>(teamSet);
    }

    public void calculate(ScoreCard previousScoreCard) {
        IntStream.range(0, driverList.size())
                .parallel()
                .mapToObj(i ->
                        new AbstractMap.SimpleEntry<FullPointEntity, List<FullPointEntity>>(driverList.get(i),
                                new ArrayList<>(driverList.subList(i + 1, driverList.size()))))
                .forEach(driver -> {
                    driverLoop(List.of(driver.getKey()), driver.getValue());
                    System.out.println("Driver " + driver.getKey().getName() + " done");
                });

        System.out.println("Number of valid combinations: " + validTeamSet.size());

        System.out.println("sorting");


        System.out.println("Previous scorecard: " + previousScoreCard);

        scoreCardOutput(previousScoreCard, Comparator.comparing(ScoreCard::getScore));
    }

    private void scoreCardOutput(ScoreCard currentScorecard, Comparator<ScoreCard> comparing) {
        System.out.println("\nAbsolute Score Cards: ----------------------------------------------------------");

        List<ScoreCard> scoreCardList = validTeamSet.stream().sorted(comparing.reversed()).limit(30).toList();

        scoreCardList.forEach(System.out::println);

        System.out.println("\nDifference Entities: ----------------------------------------------------------");

        List<DifferenceEntity> differenceEntityList = scoreCardList.stream().map(currentScorecard::calculateDifference).toList();
        differenceEntityList = differenceEntityList.stream().filter(sc -> sc.getNumberOfChanges() <= getTransferLimit()).toList();

        differenceEntityList.forEach(System.out::println);
    }

    private void driverLoop(
            List<FullPointEntity> previousLevelDriverList, List<FullPointEntity> loopDriverList) {
        if (previousLevelDriverList.size() == 5) {
            if (getTeamCost(previousLevelDriverList) <= getCostCap()) {
                teamLoop(new ArrayList<>(), previousLevelDriverList, teamList);
            }
        } else {
            for (int i = 0; i < loopDriverList.size(); i++) {
                List<FullPointEntity> newDriverList = new ArrayList<>(loopDriverList.subList(i + 1, loopDriverList.size()));
                if (newDriverList.size() + previousLevelDriverList.size() >= 5) {
                    List<FullPointEntity> nextLevelDriverList = new ArrayList<>(previousLevelDriverList);
                    nextLevelDriverList.add(loopDriverList.get(i));

                    if (getTeamCost(nextLevelDriverList) <= getCostCap()) {
                        driverLoop(nextLevelDriverList, newDriverList);
                    }

                }
            }
        }
    }

    private void teamLoop(
            List<FullPointEntity> previousLevelTeamList, List<FullPointEntity> driverList, List<FullPointEntity> loopTeamList) {
        if (previousLevelTeamList.size() == 2) {
            if (getTeamCost(driverList, previousLevelTeamList) <= getCostCap() && getTeamCost(driverList, previousLevelTeamList) > getCostCap() - 5) {
                ScoreCard scoreCard = new ScoreCard(driverList, previousLevelTeamList, getRaceName(), getCostCap());
                validTeamSet.add(scoreCard);
            }

        } else {
            for (int i = 0; i < loopTeamList.size(); i++) {
                List<FullPointEntity> newTeamList = new ArrayList<>(loopTeamList.subList(i + 1, loopTeamList.size()));
                if (newTeamList.size() + previousLevelTeamList.size() >= 2) {
                    List<FullPointEntity> nextLevelTeamList = new ArrayList<>(previousLevelTeamList);
                    nextLevelTeamList.add(loopTeamList.get(i));
                    if (getTeamCost(driverList, nextLevelTeamList) <= getCostCap()) {
                        teamLoop(nextLevelTeamList, driverList, newTeamList);
                    }
                }
            }

        }
    }

    @SafeVarargs
    private double getTeamCost(List<? extends BasicPointEntity>... entityList) {
        double currentCost = 0D;

        for (List<? extends BasicPointEntity> basicPointEntityList : entityList) {
            for (BasicPointEntity basicPointEntity : basicPointEntityList) {
                currentCost += basicPointEntity.getCost();
            }
        }
        return currentCost;
    }

    public ScoreCard createPreviousScoreCard(List<String> driverNames, List<String> teamNames) {
        ScoreCard scoreCard = new ScoreCard();
        getDriverSet().stream().filter(d -> driverNames.contains(d.getName())).forEach(scoreCard::addDriver);
        getTeamSet().stream().filter(t -> teamNames.contains(t.getName())).forEach(scoreCard::addTeam);
        scoreCard.intialize(getRaceName(), getCostCap());
        return scoreCard;
    }
}
