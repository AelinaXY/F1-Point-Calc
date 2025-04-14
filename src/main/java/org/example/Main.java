package org.example;

import java.util.*;

public class Main {

    private static final List<PointEntity> ALL_DRIVERLIST = new ArrayList<>(
            List.of(
                    new PointEntity("Alex Albon", 13, 12),
                    new PointEntity("Andrea Kimi Antonelli", 19.6, 28.0),
                    new PointEntity("Carlos Sainz", 11.3, -2.3),
                    new PointEntity("Charles Leclerc", 25, 6.7),
                    new PointEntity("Estaban Ocon", 8.7, 11),
                    new PointEntity("Fernando Alonso", 7.6, -10.7),
                    new PointEntity("Gabriel Bortoleto", 4.5, -4.3),
                    new PointEntity("George Russell", 21.7, 26),
                    new PointEntity("Isack Hadjar", 4.5, -0.3),
                    new PointEntity("Jack Doohan", 5.4, -3),
                    new PointEntity("Lance Stroll", 9.9, 11.7),
                    new PointEntity("Lando Norris", 29.9, 42.3),
                    new PointEntity("Lewis Hamilton", 23.3, 4.7),
                    new PointEntity("Liam Lawson", 7.8, -1.3),
                    new PointEntity("Max Verstappen", 28.7, 31.7),
                    new PointEntity("Nico Hulkenberg", 7.8, 7.7),
                    new PointEntity("Oliver Bearman", 7.3, 8.7),
                    new PointEntity("Oscar Piastri", 23.1, 26.3),
                    new PointEntity("Pierre Gasly", 10, -2.3),
                    new PointEntity("Yuki Tsunoda", 16.6, 4.3)
            ));

    private static final List<PointEntity> ALL_TEAMLIST = new ArrayList<>(
            List.of(
                    new PointEntity("Alpine", 7.7, -3.7),
                    new PointEntity("Aston Martin", 7.1, 4.7),
                    new PointEntity("Ferrari", 27, 23.0),
                    new PointEntity("Haas", 8, 23.7),
                    new PointEntity("Kick Sauber", 6, 5),
                    new PointEntity("Mclaren", 30.9, 81),
                    new PointEntity("Mercedes", 23.6, 64),
                    new PointEntity("Racing Bulls", 8.6, 15.3),
                    new PointEntity("Red Bull Racing", 25.7, 44.0),
                    new PointEntity("Williams", 14.1, 18.3)
            )
    );

    private static final Set<ScoreCard> validTeamSet = new HashSet<>();

    public static void main(String[] args) {

        ALL_DRIVERLIST.parallelStream().forEach(driver -> {
            driverLoop(Set.of(driver));
            System.out.println("Driver " + driver.getName() + " done +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
        });

        System.out.println("sorting");
        List<ScoreCard> validTeamlist = validTeamSet.stream().sorted(Comparator.comparing(ScoreCard::getAveragePoints).reversed()).limit(200).toList();

        System.out.println(new ScoreCard(Set.of(new PointEntity("Jack Doohan", 5.4, -3), new PointEntity("Gabriel Bortoleto", 4.5, -4.3), new PointEntity("Isack Hadjar", 4.5, -0.3), new PointEntity("George Russell", 21.7, 26), new PointEntity("Oliver Bearman", 7.3, 8.7)),
                Set.of(new PointEntity("Mclaren", 30.9, 81), new PointEntity("Mercedes", 23.6, 64))));

        for (int i = 0; i < validTeamlist.size(); i++) {
            System.out.println(i + ": " + validTeamlist.get(i));
        }
    }

    private static void driverLoop(
            Set<PointEntity> previousLevelDriverSet) {
        if (previousLevelDriverSet.size() == 5) {
            if (!(previousLevelDriverSet.stream().map(PointEntity::getCost).reduce(0d, Double::sum) >= 98)) {
                teamLoop(new HashSet<>(), previousLevelDriverSet);
            }
        } else {
            for (PointEntity driver : ALL_DRIVERLIST) {
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
            if (scoreCard.getCost() <= 98 && scoreCard.getCost() > 94) {
                validTeamSet.add(scoreCard);
            }

        } else {
            for (PointEntity team : ALL_TEAMLIST) {
                if (!previousLevelTeamSet.contains(team)) {
                    Set<PointEntity> nextLevelTeamSet = new HashSet<>(previousLevelTeamSet);
                    nextLevelTeamSet.add(team);
                    teamLoop(nextLevelTeamSet, driverSet);
                }
            }
        }
    }
}