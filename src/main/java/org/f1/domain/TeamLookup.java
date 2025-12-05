package org.f1.domain;

import java.util.List;

public enum TeamLookup {

    RACING_BULLS("Racing Bulls", List.of("VRacing Bulls", "VCARB", "AlphaTauri"), List.of("AlphaTauri", "RB", "Racing Bulls")),
    ASTON_MARTIN("Aston Martin", List.of("Aston Martin"), List.of("Aston Martin")),
    MERCEDES("Mercedes", List.of("Mercedes"), List.of("Mercedes")),
    WILLIAMS("Williams", List.of("Williams"), List.of("Williams")),
    RED_BULL_RACING("Red Bull Racing", List.of("Red Bull Racing"), List.of("Red Bull Racing")),
    KICK_SAUBER("Kick Sauber", List.of("Kick Sauber", "Alfa Romeo"), List.of("Kick Sauber", "Alfa Romeo")),
    ALPINE("Alpine", List.of("Alpine"), List.of("Alpine")),
    HAAS("Haas", List.of("Haas"), List.of("Haas F1 Team")),
    FERRARI("Ferrari", List.of("Ferrari"), List.of("Ferrari")),
    MCLAREN("Mclaren", List.of("Mclaren"), List.of("McLaren"));

    private final String teamName;
    private final List<String> csvNames;
    private final List<String> apiNames;

    TeamLookup(String teamName, List<String> csvNames, List<String> apiNames) {
        this.teamName = teamName;
        this.csvNames = csvNames;
        this.apiNames = apiNames;
    }

    public static String apiToPreferred(String teamName) {
        if (teamName == null) {
            return null;
        }
        for (TeamLookup teamLookup : TeamLookup.values()) {
            if (teamLookup.apiNames.contains(teamName)) {
                return teamLookup.teamName;
            }
        }
        return null;
    }

    public static String csvToPreferred(String teamName) {
        for (TeamLookup teamLookup : TeamLookup.values()) {
            if (teamLookup.csvNames.contains(teamName)) {
                return teamLookup.teamName;
            }
        }
        return null;
    }
}
