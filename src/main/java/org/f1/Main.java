package org.f1;

import org.f1.domain.DifferenceEntity;
import org.f1.domain.BasicPointEntity;
import org.f1.domain.ScoreCard;
import org.f1.parsing.CSVParsing;

import java.util.*;
import java.util.stream.Collectors;

public class Main {

    private static Set<? extends BasicPointEntity> DRIVER_SET = CSVParsing.oldParse("Drivers_Output.csv");
    private static Set<? extends BasicPointEntity> TEAM_SET = CSVParsing.oldParse("Teams_Output.csv");


    private static final Set<ScoreCard> validTeamSet = new HashSet<>();
    public static final double COST_CAP = 102.7;
    public static final long TRANSFER_LIMIT = 3L;

    public static void main(String[] args) {
        //Drivers no longer driving
        List<String> driversNoLongerExists = List.of("Jack Doohan");
        DRIVER_SET = DRIVER_SET.stream().filter(d -> !driversNoLongerExists.contains(d.getName())).collect(Collectors.toSet());


        DRIVER_SET.parallelStream().forEach(driver -> {
            driverLoop(Set.of(driver));
            System.out.println("Driver " + driver.getName() + " done");
        });

        System.out.println("Number of valid combinations: " + validTeamSet.size());

        System.out.println("sorting");

        ScoreCard previousScoreCard = createPreviousScoreCard(List.of("Franco Colapinto", "Liam Lawson", "Isack Hadjar", "Oscar Piastri", "Nico Hulkenberg"), List.of("Mclaren", "Mercedes"));

        System.out.println("Previous scorecard: " + previousScoreCard);

        scoreCardOutput(previousScoreCard, Comparator.comparing(ScoreCard::getAveragePoints));

        scoreCardOutput(previousScoreCard, Comparator.comparing(ScoreCard::getThreeRaceAveragePoints));
    }

    private static void scoreCardOutput(ScoreCard currentScorecard, Comparator<ScoreCard> comparing) {
        System.out.println("\nAbsolute Score Cards: ----------------------------------------------------------");

        List<ScoreCard> scoreCardList = validTeamSet.stream().sorted(comparing.reversed()).limit(30).toList();

        scoreCardList.forEach(System.out::println);

        System.out.println("\nDifference Entities: ----------------------------------------------------------");

        List<DifferenceEntity> differenceEntityList = scoreCardList.stream().map(currentScorecard::calculateDifference).toList();
        differenceEntityList = differenceEntityList.stream().filter(sc -> sc.getNumberOfChanges() <= TRANSFER_LIMIT).toList();

        differenceEntityList.forEach(System.out::println);
    }

    private static void driverLoop(
            Set<BasicPointEntity> previousLevelDriverSet) {
        if (previousLevelDriverSet.size() == 5) {
            if (!(previousLevelDriverSet.stream().map(BasicPointEntity::getCost).reduce(0d, Double::sum) >= COST_CAP)) {
                teamLoop(new HashSet<>(), previousLevelDriverSet);
            }
        } else {
            for (BasicPointEntity driver : DRIVER_SET) {
                if (!previousLevelDriverSet.contains(driver)) {
                    Set<BasicPointEntity> nextLevelDriverSet = new HashSet<>(previousLevelDriverSet);
                    nextLevelDriverSet.add(driver);
                    driverLoop(nextLevelDriverSet);
                }
            }
        }
    }

    private static void teamLoop(
            Set<BasicPointEntity> previousLevelTeamSet, Set<BasicPointEntity> driverSet) {
        if (previousLevelTeamSet.size() == 2) {
            ScoreCard scoreCard = new ScoreCard(driverSet, previousLevelTeamSet);
            if (scoreCard.getCost() <= COST_CAP && scoreCard.getCost() > 94) {
                validTeamSet.add(scoreCard);
            }

        } else {
            for (BasicPointEntity team : TEAM_SET) {
                if (!previousLevelTeamSet.contains(team)) {
                    Set<BasicPointEntity> nextLevelTeamSet = new HashSet<>(previousLevelTeamSet);
                    nextLevelTeamSet.add(team);
                    teamLoop(nextLevelTeamSet, driverSet);
                }
            }
        }
    }

    private static ScoreCard createPreviousScoreCard(List<String> driverNames, List<String> teamNames){
        ScoreCard scoreCard = new ScoreCard();
        DRIVER_SET.stream().filter(d -> driverNames.contains(d.getName())).forEach(scoreCard::addDriver);
        TEAM_SET.stream().filter(t -> teamNames.contains(t.getName())).forEach(scoreCard::addTeam);
        scoreCard.intialize();
        return scoreCard;
    }
}