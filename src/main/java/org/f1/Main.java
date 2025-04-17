package org.f1;

import org.f1.enums.Drivers;
import org.f1.enums.Teams;

import java.util.*;

public class Main {

    private static final List<Drivers> ALL_DRIVERLIST = new ArrayList<>(List.of(Drivers.values())
    );

    private static final List<Teams> ALL_TEAMLIST = new ArrayList<>(List.of(Teams.values())
    );

    private static final Set<ScoreCard> validTeamSet = new HashSet<>();
    public static final int COST_CAP = 101;

    public static void main(String[] args) {

        ALL_DRIVERLIST.parallelStream().forEach(driver -> {
            driverLoop(Set.of(driver));
            System.out.println("Driver " + driver.getPointEntity().getName() + " done");
        });

        System.out.println("sorting");

        ScoreCard currentScorecard = new ScoreCard(Set.of(Drivers.DOO, Drivers.BOR, Drivers.HAD, Drivers.RUS, Drivers.BEA),
                Set.of(Teams.MCL, Teams.MER));

        System.out.println(currentScorecard);

        List<ScoreCard> scoreCardList = validTeamSet.stream().sorted(Comparator.comparing(ScoreCard::getAveragePoints).reversed()).limit(50).toList();

        scoreCardList.forEach(System.out::println);

        System.out.println("----------------------------------------------------------");

        List<DifferenceEntity> differenceEntityList = scoreCardList.stream().map(currentScorecard::calculateDifference).toList();
        differenceEntityList = differenceEntityList.stream().filter(sc -> sc.getNumberOfChanges() <= 2L).toList();

        differenceEntityList.forEach(System.out::println);
    }

    private static void driverLoop(
            Set<Drivers> previousLevelDriverSet) {
        if (previousLevelDriverSet.size() == 5) {
            if (!(previousLevelDriverSet.stream().map(driver -> driver.getPointEntity().getCost()).reduce(0d, Double::sum) >= COST_CAP)) {
                teamLoop(new HashSet<>(), previousLevelDriverSet);
            }
        } else {
            for (Drivers driver : ALL_DRIVERLIST) {
                if (!previousLevelDriverSet.contains(driver)) {
                    Set<Drivers> nextLevelDriverSet = new HashSet<>(previousLevelDriverSet);
                    nextLevelDriverSet.add(driver);
                    driverLoop(nextLevelDriverSet);
                }
            }
        }
    }

    private static void teamLoop(
            Set<Teams> previousLevelTeamSet, Set<Drivers> driverSet) {
        if (previousLevelTeamSet.size() == 2) {
            ScoreCard scoreCard = new ScoreCard(driverSet, previousLevelTeamSet);
            if (scoreCard.getCost() <= COST_CAP && scoreCard.getCost() > 94) {
                validTeamSet.add(scoreCard);
            }

        } else {
            for (Teams team : ALL_TEAMLIST) {
                if (!previousLevelTeamSet.contains(team)) {
                    Set<Teams> nextLevelTeamSet = new HashSet<>(previousLevelTeamSet);
                    nextLevelTeamSet.add(team);
                    teamLoop(nextLevelTeamSet, driverSet);
                }
            }
        }
    }
}