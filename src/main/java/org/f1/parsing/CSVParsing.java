package org.f1.parsing;

import org.f1.domain.FullPointEntity;
import org.f1.domain.Race;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static org.f1.domain.Race.RaceBuilder.aRaceBuilder;

public class CSVParsing {

    public static Set<FullPointEntity> fullParse(String fileName) {
        Set<FullPointEntity> result = new HashSet<>();

        List<String> baseList = loadInput(fileName);
        String openingLine = baseList.removeFirst();

        List<String> openingLineParts = new ArrayList<>(List.of(openingLine.split(",")));
        openingLineParts.removeFirst();
        openingLineParts.removeFirst();
        openingLineParts = openingLineParts.subList(0, openingLineParts.size() - 6);

        List<Integer> tempIntegerList = new ArrayList<>();
        LinkedHashMap<String, List<Integer>> raceMap = new LinkedHashMap<>();
        for (int i = 0; i < openingLineParts.size(); i++) {
            if (openingLineParts.get(i).contains(":")) {
                tempIntegerList.add(i);
            } else {
                raceMap.put(openingLineParts.get(i), new ArrayList<>(tempIntegerList));
                tempIntegerList = new ArrayList<>();
            }
        }

        for (String line : baseList) {
            List<String> parts = new ArrayList<>(List.of(line.split(",")));
            parts = parts.stream().map(value -> {
                if (value.isEmpty()) {
                    return "0";
                }
                return value;
            }).collect(Collectors.toList());

            String name = parts.removeFirst();
            Double cost = Double.parseDouble(parts.removeFirst());

            List<String> raceNames = raceMap.sequencedKeySet().stream().toList();
            List<Race> races = new ArrayList<>();

            for (int i = 0; i < raceMap.size(); i++) {
                Race.RaceBuilder currentRaceBuilder = aRaceBuilder();
                String raceName = raceNames.get(i);

                List<Integer> racePointLocations = raceMap.get(raceName);
                currentRaceBuilder.withName(raceName).withRaceNumber(i);

                if (racePointLocations.size() == 3) {
                    currentRaceBuilder.withSprintPoints(Double.valueOf(parts.get(racePointLocations.get(0))))
                            .withQualiPoints(Double.valueOf(parts.get(racePointLocations.get(1))))
                            .withRacePoints(Double.valueOf(parts.get(racePointLocations.get(2))));
                } else {
                    currentRaceBuilder.withQualiPoints(Double.valueOf(parts.get(racePointLocations.get(0))))
                            .withRacePoints(Double.valueOf(parts.get(racePointLocations.get(1))));
                }
                races.add(currentRaceBuilder.build());
            }
            result.add(new FullPointEntity(name, cost, races));
        }

        return result;
    }

    private static List<String> loadInput(String fileName) {

        InputStream inputForDay = ClassLoader.getSystemResourceAsStream(fileName);
        if (Objects.isNull(inputForDay)) {
            throw new IllegalArgumentException("Can’t find data using filename: " + fileName + ". Did you forget to put the file in the resources directory?");
        }
        try (BufferedReader r = new BufferedReader(new InputStreamReader(inputForDay))) {
            return r.lines().collect(toList());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
