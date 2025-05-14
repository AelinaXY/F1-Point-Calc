package org.f1;

import org.f1.parsing.BaseCSVParsing;

import java.util.*;
import java.util.stream.Collectors;

public class Main {

    private static Set<? extends PointEntity> DRIVER_SET = BaseCSVParsing.parse("Drivers_Output.csv");
    private static Set<? extends PointEntity> TEAM_SET = BaseCSVParsing.parse("Teams_Output.csv");


    private static final Set<ScoreCard> validTeamSet = new HashSet<>();
    public static final double COST_CAP = 108.5;
    public static final long TRANSFER_LIMIT = 3L;

    public static void main(String[] args) {
        //Drivers no longer driving
        List<String> driversNoLongerExists = List.of("Jack Doohan");
        DRIVER_SET = DRIVER_SET.stream().filter(d -> !driversNoLongerExists.contains(d.getName())).collect(Collectors.toSet());

        DRIVER_SET.parallelStream().forEach(driver -> {
            driverLoop(Set.of(driver));
            System.out.println("Driver " + driver.getName() + " done");
        });

        System.out.println(validTeamSet.size());

        System.out.println("sorting");

        ScoreCard previousScoreCard = createPreviousScoreCard(List.of("Franco Colapinto", "Gabriel Bortoleto", "Isack Hadjar", "Oscar Piastri", "Liam Lawson"), List.of("Mclaren", "Mercedes"));

        System.out.println(previousScoreCard);

        scoreCardOutput(previousScoreCard, Comparator.comparing(ScoreCard::getAveragePoints));

        scoreCardOutput(previousScoreCard, Comparator.comparing(ScoreCard::getThreeRaceAveragePoints));
    }

    private static void scoreCardOutput(ScoreCard currentScorecard, Comparator<ScoreCard> comparing) {
        System.out.println("----------------------------------------------------------");

        List<ScoreCard> scoreCardList = validTeamSet.stream().sorted(comparing.reversed()).limit(30).toList();

        scoreCardList.forEach(System.out::println);

        System.out.println("----------------------------------------------------------");

        List<DifferenceEntity> differenceEntityList = scoreCardList.stream().map(currentScorecard::calculateDifference).toList();
        differenceEntityList = differenceEntityList.stream().filter(sc -> sc.getNumberOfChanges() <= TRANSFER_LIMIT).toList();

        differenceEntityList.forEach(System.out::println);
    }

    private static void driverLoop(
            Set<PointEntity> previousLevelDriverSet) {
        if (previousLevelDriverSet.size() == 5) {
            if (!(previousLevelDriverSet.stream().map(PointEntity::getCost).reduce(0d, Double::sum) >= COST_CAP)) {
                teamLoop(new HashSet<>(), previousLevelDriverSet);
            }
        } else {
            for (PointEntity driver : DRIVER_SET) {
                if (!previousLevelDriverSet.contains(driver)) {
                    Set<PointEntity> nextLevelDriverSet = new HashSet<>(previousLevelDriverSet);
                    nextLevelDriverSet.add(driver);
                    driverLoop(nextLevelDriverSet);
                }
            }
        }
    }

    private static void teamLoop(
            Set<PointEntity> previousLevelTeamSet, Set<PointEntity> driverSet) {
        if (previousLevelTeamSet.size() == 2) {
            ScoreCard scoreCard = new ScoreCard(driverSet, previousLevelTeamSet);
            if (scoreCard.getCost() <= COST_CAP && scoreCard.getCost() > 94) {
                validTeamSet.add(scoreCard);
            }

        } else {
            for (PointEntity team : TEAM_SET) {
                if (!previousLevelTeamSet.contains(team)) {
                    Set<PointEntity> nextLevelTeamSet = new HashSet<>(previousLevelTeamSet);
                    nextLevelTeamSet.add(team);
                    teamLoop(nextLevelTeamSet, driverSet);
                }
            }
        }
    }

    private static ScoreCard createPreviousScoreCard(List<String> driverNames, List<String> teamNames){
        ScoreCard scoreCard = new ScoreCard();
        for (String driverName : driverNames) {
            scoreCard.addDriver(DRIVER_SET.stream().filter(d -> d.getName().equals(driverName)).findFirst().orElse(null));
        }
        for (String teamName : teamNames) {
            scoreCard.addTeam(TEAM_SET.stream().filter(t -> t.getName().equals(teamName)).findFirst().orElse(null));
        }
        scoreCard.intialize();
        return scoreCard;
    }
}