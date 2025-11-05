package org.f1.domain;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public enum Meeting {
    ABU_DHABI("Abu Dhabi", List.of("Abu Dhabi Grand Prix"), List.of(2023, 2024, 2025), List.of()),
    AUSTRALIA("Australia", List.of("Australian Grand Prix"), List.of(2023, 2024, 2025), List.of()),
    AUSTRIA("Austria", List.of("Austrian Grand Prix"), List.of(2023, 2024, 2025), List.of(2023, 2024)),
    AZERBAIJAN("Azerbaijan", List.of("Azerbaijan Grand Prix"), List.of(2023, 2024, 2025), List.of(2023)),
    BAHRAIN("Bahrain", List.of("Bahrain Grand Prix"), List.of(2023, 2024, 2025), List.of()),
    BELGIUM("Belgium", List.of("Belgian Grand Prix"), List.of(2023, 2024, 2025), List.of(2023, 2025)),
    BRAZIL("Brazil", List.of("São Paulo Grand Prix"), List.of(2023, 2024, 2025), List.of(2023, 2024, 2025)),
    BRITAIN("Britain", List.of("British Grand Prix"), List.of(2023, 2024, 2025), List.of()),
    CANADA("Canada", List.of("Canadian Grand Prix"), List.of(2023, 2024, 2025), List.of()),
    CHINA("China", List.of("Chinese Grand Prix"), List.of(2024, 2025), List.of(2024, 2025)),
    HUNGARY("Hungary", List.of("Hungarian Grand Prix"), List.of(2023, 2024, 2025), List.of()),
    IMOLA("Imola", List.of("Emilia Romagna Grand Prix"), List.of(2024, 2025), List.of()),
    ITALY("Italy", List.of("Italian Grand Prix"), List.of(2023, 2024, 2025), List.of()),
    JAPAN("Japan", List.of("Japanese Grand Prix"), List.of(2023, 2024, 2025), List.of()),
    LAS_VEGAS("Las Vegas", List.of("Las Vegas Grand Prix"), List.of(2023, 2024, 2025), List.of()),
    MEXICO("Mexico", List.of("Mexico City Grand Prix"), List.of(2023, 2024, 2025), List.of()),
    MIAMI("Miami", List.of("Miami Grand Prix"), List.of(2023, 2024, 2025), List.of(2024, 2025)),
    MONACO("Monaco", List.of("Monaco Grand Prix"), List.of(2023, 2024, 2025), List.of()),
    NETHERLANDS("Netherlands", List.of("Dutch Grand Prix"), List.of(2023, 2024, 2025), List.of()),
    QATAR("Qatar", List.of("Qatar Grand Prix"), List.of(2023, 2024, 2025), List.of(2023, 2024, 2025)),
    SAUDI("Saudi Arabia", List.of("Saudi Arabian Grand Prix"), List.of(2023, 2024, 2025), List.of()),
    SINGAPORE("Singapore", List.of("Singapore Grand Prix"), List.of(2023, 2024, 2025), List.of()),
    SPAIN("Spain", List.of("Spanish Grand Prix"), List.of(2023, 2024, 2025), List.of()),
    UNITED_STATES("United States", List.of("United States Grand Prix"), List.of(2023, 2024, 2025), List.of(2023, 2024, 2025));

    Meeting(String shortName, List<String> fullNames, List<Integer> years, List<Integer> sprintYears) {
        this.shortName = shortName;
        this.fullNames = fullNames;
        this.years = years;
        this.sprintYears = sprintYears;
    }

    private final String shortName;
    private final List<String> fullNames;
    private final List<Integer> years;
    private final List<Integer> sprintYears;

    public static Set<Meeting> getMeetings(int year) {
        return Arrays.stream(Meeting.values()).filter(meeting -> meeting.years.contains(year)).collect(Collectors.toSet());
    }

    public static Set<Meeting> getNonSprintMeetings(int year) {
        return Arrays.stream(Meeting.values()).filter(meeting -> meeting.years.contains(year) && !meeting.sprintYears.contains(year)).collect(Collectors.toSet());
    }

    public String getShortName() {
        return shortName;
    }

    public List<String> getFullNames() {
        return fullNames;
    }

    public List<Integer> getYears() {
        return years;
    }
}
