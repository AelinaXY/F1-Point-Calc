package org.f1.calculations;

import org.f1.domain.BasicPointEntity;
import org.f1.domain.DifferenceEntity;
import org.f1.domain.FullPointEntity;
import org.f1.domain.ScoreCard;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArraySet;

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
        driverList.parallelStream()
                .map(d ->
                        new AbstractMap.SimpleEntry<FullPointEntity, List<FullPointEntity>>(d,
                                new ArrayList<>(driverList.subList(driverList.indexOf(d)+1, driverList.size()))))
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
            List<FullPointEntity> previousLevelDriverSet, List<FullPointEntity> loopDriverList) {
        if (previousLevelDriverSet.size() == 5) {
            if (!(previousLevelDriverSet.stream().map(BasicPointEntity::getCost).reduce(0d, Double::sum) >= getCostCap())) {
                teamLoop(new ArrayList<>(), previousLevelDriverSet, teamList);
            }
        } else {
            for (FullPointEntity driver : loopDriverList) {
                List<FullPointEntity> newDriverList = new ArrayList<>(loopDriverList.subList(loopDriverList.indexOf(driver)+1, loopDriverList.size()));
                if (newDriverList.size() + previousLevelDriverSet.size() >= 5) {
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
            ScoreCard scoreCard = new ScoreCard(new HashSet<>(driverSet), new HashSet<>(previousLevelTeamSet), getRaceName(), getCostCap());
            if (scoreCard.getCost() <= getCostCap() && scoreCard.getCost() > 94) {
                validTeamSet.add(scoreCard);
            }

        } else {
            for (FullPointEntity team : loopTeamList) {
                List<FullPointEntity> newTeamList = new ArrayList<>(loopTeamList.subList(loopTeamList.indexOf(team), loopTeamList.size()));
                if (newTeamList.size() + previousLevelTeamSet.size() >= 2) {
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
        scoreCard.intialize(getRaceName(), getCostCap());
        return scoreCard;
    }
}
