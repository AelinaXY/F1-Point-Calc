package org.f1.parsing;

import org.f1.domain.FullPointEntity;
import org.f1.domain.Race;
import org.f1.domain.Track;
import org.f1.domain.EntityType;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static org.f1.domain.Race.RaceBuilder.aRaceBuilder;
import static org.f1.domain.Track.TrackBuilder.aTrackBuilder;

public class CSVParsing {

    public static final int GARBAGE_COLUMNS = 7;

    public static Set<FullPointEntity> parseFullPointEntities(String fileName, EntityType entityType) {
        Set<FullPointEntity> result = new HashSet<>();

        List<String> baseList = loadInput(fileName);
        String openingLine = baseList.removeFirst();

        List<String> openingLineParts = new ArrayList<>(List.of(openingLine.split(",")));
        openingLineParts.removeFirst();
        openingLineParts.removeFirst();
        openingLineParts.removeFirst();

        openingLineParts = openingLineParts.subList(0, openingLineParts.size() - GARBAGE_COLUMNS);

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
                    return "INVALID VALUE";
                }
                return value;
            }).collect(Collectors.toList());

            String name = parts.removeFirst();
            Double baseCost = Double.parseDouble(parts.removeFirst());
            Double cost = Double.parseDouble(parts.removeFirst());

            List<String> raceNames = raceMap.sequencedKeySet().stream().toList();
            List<Race> races = new ArrayList<>();

            for (int i = 0; i < raceMap.size(); i++) {
                Race.RaceBuilder currentRaceBuilder = aRaceBuilder();
                String raceName = raceNames.get(i);

                boolean invalidFlag = false;
                List<Integer> racePointLocations = raceMap.get(raceName);
                for (Integer location : racePointLocations) {
                    if (parts.get(location).equals("INVALID VALUE")) {
                        invalidFlag = true;
                    }
                }
                if (invalidFlag) {
                    continue;
                }

                currentRaceBuilder = currentRaceBuilder.withName(raceName).withRaceNumber(i);

                if (racePointLocations.size() == 3) {
                    currentRaceBuilder = currentRaceBuilder.withSprintPoints(Double.valueOf(parts.get(racePointLocations.get(0))))
                            .withQualiPoints(Double.valueOf(parts.get(racePointLocations.get(1))))
                            .withRacePoints(Double.valueOf(parts.get(racePointLocations.get(2))));
                } else {
                    currentRaceBuilder = currentRaceBuilder.withQualiPoints(Double.valueOf(parts.get(racePointLocations.get(0))))
                            .withRacePoints(Double.valueOf(parts.get(racePointLocations.get(1))));
                }
                races.add(currentRaceBuilder.build());
            }
            result.add(new FullPointEntity(name, cost, races, entityType, baseCost));
        }

        return result;
    }

    public static Map<String, Track> parseTracks(String fileName) {
        Map<String, Track> result = new HashMap<>();

        List<String> baseList = loadInput(fileName);
        String openingLine = baseList.removeFirst();


        for (String line : baseList) {
            List<String> values = List.of(line.split(","));
            Track.TrackBuilder trackBuilder = aTrackBuilder();

            trackBuilder
                    .withTrackName(values.get(0))
                    .withAvgTemp(Double.valueOf(values.get(1)))
                    .withAvgRain(Double.valueOf(values.get(2)))
                    .withTrackLength(Double.valueOf(values.get(3)))
                    .withPitLaneTimeLoss(Double.valueOf(values.get(4)))
                    .withFastestLap(Double.valueOf(values.get(5)))
                    .withTopSpeed(Double.valueOf(values.get(6)))
                    .withNumberOfCorners(Double.valueOf(values.get(7)))
                    .withAvgOvertakes(Double.valueOf(values.get(8)));

            Track track = trackBuilder.build();

            result.put(track.getTrackName(), track);
        }

        List<Track> trackList = result.values().stream().toList();


        updateTrackDistances(trackList, result, List.of(-1d, 0.2d, 31d, 4.2d, -2.6d, 10.4d, 1.8d, 4.8d));

        return result;
    }

    public static void updateTrackDistances(List<Track> trackList, Map<String, Track> result, List<Double> weightList) {
        for (Track track1 : trackList) {
            List<Track> subsequentTrackList = new ArrayList<>(trackList.subList(trackList.indexOf(track1) + 1, trackList.size()));
            for (Track track2 : subsequentTrackList) {

                double distance = calculateDistance(track1, track2, weightList);

                double similarity = 1 - distance;

                result.get(track1.getTrackName()).addTrackRelationship(track2.getTrackName(), similarity);
                result.get(track2.getTrackName()).addTrackRelationship(track1.getTrackName(), similarity);
            }
        }
    }

    private static double calculateDistance(Track track1, Track track2, List<Double> weightList) {
        double runningTotal = 0.0;

        runningTotal += Math.pow(track1.getAvgTemp() - track2.getAvgTemp(), 2) * weightList.get(0);
        runningTotal += Math.pow(track1.getAvgRain() - track2.getAvgRain(), 2) * weightList.get(1);
        runningTotal += Math.pow(track1.getTrackLength() - track2.getTrackLength(), 2) * weightList.get(2);
        runningTotal += Math.pow(track1.getPitLaneTimeLoss() - track2.getPitLaneTimeLoss(), 2) * weightList.get(3);
        runningTotal += Math.pow(track1.getFastestLap() - track2.getFastestLap(), 2) * weightList.get(4);
        runningTotal += Math.pow(track1.getTopSpeed() - track2.getTopSpeed(), 2) * weightList.get(5);
        runningTotal += Math.pow(track1.getNumberOfCorners() - track2.getNumberOfCorners(), 2) * weightList.get(6);
        runningTotal += Math.pow(track1.getAvgOvertakes() - track2.getAvgOvertakes(), 2) * weightList.get(7);


        return Math.sqrt(runningTotal);
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
