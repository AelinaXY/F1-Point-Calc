package org.example;

import java.util.*;

public class Main {

    private static final List<PointEntity> ALL_DRIVERLIST = new ArrayList<>(
            List.of(
                    new PointEntity("Alex Albon", 12.8, 12.8),
                    new PointEntity("Andrea Kimi Antonelli", 19.7, 24.5),
                    new PointEntity("Carlos Sainz", 10.7, -4),
                    new PointEntity("Charles Leclerc", 24.7, 10),
                    new PointEntity("Estaban Ocon", 9.3, 12),
                    new PointEntity("Fernando Alonso", 6.4, -6.8),
                    new PointEntity("Gabriel Bortoleto", 4.5, -2.3),
                    new PointEntity("George Russell", 22, 27.3),
                    new PointEntity("Isack Hadjar", 5.1, 1),
                    new PointEntity("Jack Doohan", 5.6, -0.3),
                    new PointEntity("Lance Stroll", 9.7, 10.5),
                    new PointEntity("Lando Norris", 30, 39.8),
                    new PointEntity("Lewis Hamilton", 23.2, 13),
                    new PointEntity("Liam Lawson", 7.2, -3.5),
                    new PointEntity("Max Verstappen", 28.8, 29.3),
                    new PointEntity("Nico Hulkenberg", 7.2, 0.8),
                    new PointEntity("Oliver Bearman", 7.9, 12),
                    new PointEntity("Oscar Piastri", 23.4, 31),
                    new PointEntity("Pierre Gasly", 9.4, 1.5),
                    new PointEntity("Yuki Tsunoda", 16.4, 11)
            ));

    private static final List<PointEntity> ALL_TEAMLIST = new ArrayList<>(
            List.of(
                    new PointEntity("Alpine", 7.9, 2.8),
                    new PointEntity("Aston Martin", 7.7, 8),
                    new PointEntity("Ferrari", 27.3, 35.5),
                    new PointEntity("Haas", 9.4, 27.8),
                    new PointEntity("Kick Sauber", 5.4, -1.8),
                    new PointEntity("Mclaren", 31.2, 83.8),
                    new PointEntity("Mercedes", 23.9, 63),
                    new PointEntity("Racing Bulls", 9.2, 17),
                    new PointEntity("Red Bull Racing", 26, 44.5),
                    new PointEntity("Williams", 14.7, 17)
            )
    );

    private static final Set<ScoreCard> validTeamSet = new HashSet<>();
    public static final int COST_CAP = 101;

    public static void main(String[] args) {

        ALL_DRIVERLIST.parallelStream().forEach(driver -> {
            driverLoop(Set.of(driver));
            System.out.println("Driver " + driver.getName() + " done +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
        });

        System.out.println("sorting");
        System.out.println(new ScoreCard(Set.of(new PointEntity("Jack Doohan", 5.6, -0.3), new PointEntity("Gabriel Bortoleto", 4.5, -2.3), new PointEntity("Isack Hadjar", 4.5, 1), new PointEntity("George Russell", 22, 27.3), new PointEntity("Oliver Bearman", 7.9, 12)),
                Set.of(new PointEntity("Mclaren", 31.2, 83.8), new PointEntity("Mercedes", 23.9, 63))));

        validTeamSet.stream().sorted(Comparator.comparing(ScoreCard::getAveragePoints).reversed()).limit(50).forEach(System.out::println);

    }

    private static void driverLoop(
            Set<PointEntity> previousLevelDriverSet) {
        if (previousLevelDriverSet.size() == 5) {
            if (!(previousLevelDriverSet.stream().map(PointEntity::getCost).reduce(0d, Double::sum) >= COST_CAP)) {
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
            if (scoreCard.getCost() <= COST_CAP && scoreCard.getCost() > 94) {
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