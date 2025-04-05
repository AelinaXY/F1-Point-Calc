package org.example;

import java.util.*;

public class Main {

    private static final List<PointEntity> ALL_DRIVERLIST = new ArrayList<>(
            List.of(
                    new PointEntity("Estaban Ocon", 8.3, 10.7),
                    new PointEntity("Lance Stroll", 9.3, 11.0),
                    new PointEntity("Lando Norris", 29.6, 36.3),
                    new PointEntity("Oliver Bearman", 6.7, 7.7),
                    new PointEntity("Andrea Kimi Antonelli", 19.3, 22.0),
                    new PointEntity("Nico Hulkenberg", 7.6, 7.3),
                    new PointEntity("George Russell", 21.4, 22.0),
                    new PointEntity("Oscar Piastri", 23, 21.0),
                    new PointEntity("Max Verstappen", 28.6, 23.0),
                    new PointEntity("Alex Albon", 12.6, 7.3),
                    new PointEntity("Yuki Tsunoda", 9, 2.0),
                    new PointEntity("Liam Lawson", 16.8, 1.7),
                    new PointEntity("Lewis Hamilton", 23.9, 2.0),
                    new PointEntity("Charles Leclerc", 25.6, 2.3),
                    new PointEntity("Pierre Gasly", 10.6, -2.3),
                    new PointEntity("Carlos Sainz", 11.9, -3.0),
                    new PointEntity("Isack Hadjar", 5, -1.7),
                    new PointEntity("Jack Doohan", 6, -3.7),
                    new PointEntity("Gabriel Bortoleto", 4.8, -3.7),
                    new PointEntity("Fernando Alonso", 7.6, -12.0)
            ));

    private static final List<PointEntity> ALL_TEAMLIST = new ArrayList<>(
            List.of(
                    new PointEntity("Haas", 8.2, 22.3),
                    new PointEntity("Mercedes", 23.3, 52.3),
                    new PointEntity("Mclaren", 30.6, 66.3),
                    new PointEntity("Racing Bulls", 8, 13.7),
                    new PointEntity("Red Bull Racing", 25.4, 33.7),
                    new PointEntity("Williams", 13.5, 14.3),
                    new PointEntity("Kick Sauber", 6.2, 4.7),
                    new PointEntity("Aston Martin", 7.3, 2.0),
                    new PointEntity("Ferrari", 27.1, 11.0),
                    new PointEntity("Alpine", 8.9, -6.3)
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

        for(int i=0;i<validTeamlist.size(); i++)
        {
            System.out.println(i + ": " + validTeamlist.get(i));
        }
    }

    private static void driverLoop(
            Set<PointEntity> previousLevelDriverSet) {
        if (previousLevelDriverSet.size() == 5) {
            if(!(previousLevelDriverSet.stream().map(PointEntity::getCost).reduce(0d, Double::sum) >= 100))
            {
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
            if (scoreCard.getCost() <= 100 && scoreCard.getCost() > 94) {
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