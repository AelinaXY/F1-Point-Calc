package org.f1.domain;

import lombok.Getter;

import java.util.List;

@Getter
public enum TeamLookup {

    ALPINE("Alpine", List.of("Alpine"), List.of("Alpine"), 0),
    ASTON_MARTIN("Aston Martin", List.of("Aston Martin"), List.of("Aston Martin"), 1),
    AUDI("Audi", List.of("Audi", "Kick Sauber", "Alfa Romeo"), List.of("Audi", "Kick Sauber", "Alfa Romeo"), 2),
    CADILLAC("Cadillac", List.of("Cadillac"), List.of("Cadillac"), 3),
    FERRARI("Ferrari", List.of("Ferrari"), List.of("Ferrari"), 4),
    HAAS("Haas", List.of("Haas"), List.of("Haas F1 Team"), 5),
    MCLAREN("Mclaren", List.of("Mclaren"), List.of("McLaren"), 6),
    MERCEDES("Mercedes", List.of("Mercedes"), List.of("Mercedes"), 7),
    RACING_BULLS("Racing Bulls", List.of("VRacing Bulls", "VCARB", "AlphaTauri", "Racing Bulls"), List.of("AlphaTauri", "RB", "Racing Bulls"), 8),
    RED_BULL_RACING("Red Bull Racing", List.of("Red Bull Racing"), List.of("Red Bull Racing"), 9),
    WILLIAMS("Williams", List.of("Williams"), List.of("Williams"), 10),
    NONEXISTENT("Nonexistent", List.of("Nonexistent"), List.of("Nonexistent"), 11);

    private final String lineageName;
    private final List<String> csvNames;
    private final List<String> apiNames;
    private final int id;

    TeamLookup(String lineageName, List<String> csvNames, List<String> apiNames, int id) {
        this.lineageName = lineageName;
        this.csvNames = csvNames;
        this.apiNames = apiNames;
        this.id = id;
    }

    public static TeamLookup apiToTeam(String teamName) {
        if (teamName == null) {
            return NONEXISTENT;
        }
        for (TeamLookup teamLookup : TeamLookup.values()) {
            if (teamLookup.apiNames.contains(teamName)) {
                return teamLookup;
            }
        }
        return null;
    }

    public static String csvToPreferred(String teamName) {
        for (TeamLookup teamLookup : TeamLookup.values()) {
            if (teamLookup.csvNames.contains(teamName)) {
                return teamLookup.lineageName;
            }
        }
        return null;
    }
}
