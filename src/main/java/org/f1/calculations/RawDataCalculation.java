package org.f1.calculations;

import org.f1.domain.BasicPointEntity;
import org.f1.domain.DifferenceEntity;
import org.f1.domain.FullPointEntity;
import org.f1.domain.ScoreCard;

import java.util.*;

public class RawDataCalculation extends AbstractCalculation {

    private static final Set<ScoreCard> validTeamSet = new HashSet<>();

    public RawDataCalculation(Set<FullPointEntity> driverSet, Set<FullPointEntity> teamSet, double costCap, long transferLimit, String raceName, boolean isSprint) {
        super(driverSet, teamSet, costCap, transferLimit, raceName, isSprint);
    }

    public void calculate(ScoreCard previousScoreCard) {
        getDriverSet().parallelStream().forEach(driver -> {
            driverLoop(Set.of(driver));
            System.out.println("Driver " + driver.getName() + " done");
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
            Set<FullPointEntity> previousLevelDriverSet) {
        if (previousLevelDriverSet.size() == 5) {
            if (!(previousLevelDriverSet.stream().map(BasicPointEntity::getCost).reduce(0d, Double::sum) >= getCostCap())) {
                teamLoop(new HashSet<>(), previousLevelDriverSet);
            }
        } else {
            for (FullPointEntity driver : getDriverSet()) {
                if (!previousLevelDriverSet.contains(driver)) {
                    Set<FullPointEntity> nextLevelDriverSet = new HashSet<>(previousLevelDriverSet);
                    nextLevelDriverSet.add(driver);
                    driverLoop(nextLevelDriverSet);
                }
            }
        }
    }

    private void teamLoop(
            Set<FullPointEntity> previousLevelTeamSet, Set<FullPointEntity> driverSet) {
        if (previousLevelTeamSet.size() == 2) {
            ScoreCard scoreCard = new ScoreCard(new ArrayList<>(driverSet), new ArrayList<>(previousLevelTeamSet), getRaceName(), getCostCap(), isSprint(), null);
            if (scoreCard.getCost() <= getCostCap() && scoreCard.getCost() > 94) {
                validTeamSet.add(scoreCard);
            }

        } else {
            for (FullPointEntity team : getTeamSet()) {
                if (!previousLevelTeamSet.contains(team)) {
                    Set<FullPointEntity> nextLevelTeamSet = new HashSet<>(previousLevelTeamSet);
                    nextLevelTeamSet.add(team);
                    teamLoop(nextLevelTeamSet, driverSet);
                }
            }
        }
    }

    public ScoreCard createPreviousScoreCard(List<String> driverNames, List<String> teamNames) {
        ScoreCard scoreCard = new ScoreCard();
        getDriverSet().stream().filter(d -> driverNames.contains(d.getName())).forEach(scoreCard::addDriver);
        getTeamSet().stream().filter(t -> teamNames.contains(t.getName())).forEach(scoreCard::addTeam);
        scoreCard.intialize(getRaceName(), getCostCap(), isSprint(), null);
        return scoreCard;
    }
}
