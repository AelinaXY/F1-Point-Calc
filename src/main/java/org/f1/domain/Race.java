package org.f1.domain;

public record Race(String name,
                   int raceNumber,
                   Double totalPoints,
                   Double sprintPoints,
                   Double qualiPoints,
                   Double racePoints) {

    public boolean isSprint() {
        return sprintPoints != null;
    }


    public static class RaceBuilder {
        private String name;
        private int raceNumber;
        private Double sprintPoints;
        private Double qualiPoints;
        private Double racePoints;

        private RaceBuilder() {
        }

        public static RaceBuilder aRaceBuilder() {
            return new RaceBuilder();
        }

        public RaceBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public RaceBuilder withRaceNumber(int raceNumber) {
            this.raceNumber = raceNumber;
            return this;
        }

        public RaceBuilder withSprintPoints(Double sprintPoints) {
            this.sprintPoints = sprintPoints;
            return this;
        }

        public RaceBuilder withQualiPoints(Double qualiPoints) {
            this.qualiPoints = qualiPoints;
            return this;
        }

        public RaceBuilder withRacePoints(Double racePoints) {
            this.racePoints = racePoints;
            return this;
        }

        private Double calculateTotalPoints() {
            if (sprintPoints == null) {
                return qualiPoints + racePoints;
            }
            return sprintPoints + qualiPoints + racePoints;
        }

        public Race build() {
            return new Race(name, raceNumber, calculateTotalPoints(), sprintPoints, qualiPoints, racePoints);
        }
    }


}
