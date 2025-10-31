package org.f1.domain.openf1;

public record Driver(int id,
                     String broadcastName,
                     String countryCode,
                     int driverNumber,
                     String firstName,
                     String fullName,
                     String headshotUrl,
                     String lastName,
                     String nameAcronym,
                     Team team,
                     int meetingId) {


    public static final class DriverBuilder {
        private int id;
        private String broadcastName;
        private String countryCode;
        private int driverNumber;
        private String firstName;
        private String fullName;
        private String headshotUrl;
        private String lastName;
        private String nameAcronym;
        private Team team;
        private int meetingId;

        private DriverBuilder() {
        }

        public static DriverBuilder aDriver() {
            return new DriverBuilder();
        }

        public DriverBuilder withId(int id) {
            this.id = id;
            return this;
        }

        public DriverBuilder withBroadcastName(String broadcastName) {
            this.broadcastName = broadcastName;
            return this;
        }

        public DriverBuilder withCountryCode(String countryCode) {
            this.countryCode = countryCode;
            return this;
        }

        public DriverBuilder withDriverNumber(int driverNumber) {
            this.driverNumber = driverNumber;
            return this;
        }

        public DriverBuilder withFirstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public DriverBuilder withFullName(String fullName) {
            this.fullName = fullName;
            return this;
        }

        public DriverBuilder withHeadshotUrl(String headshotUrl) {
            this.headshotUrl = headshotUrl;
            return this;
        }

        public DriverBuilder withLastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public DriverBuilder withNameAcronym(String nameAcronym) {
            this.nameAcronym = nameAcronym;
            return this;
        }

        public DriverBuilder withTeam(Team team) {
            this.team = team;
            return this;
        }

        public DriverBuilder withMeetingId(int meetingId) {
            this.meetingId = meetingId;
            return this;
        }

        public Driver build() {
            return new Driver(id, broadcastName, countryCode, driverNumber, firstName, fullName, headshotUrl, lastName, nameAcronym, team, meetingId);
        }
    }

}
