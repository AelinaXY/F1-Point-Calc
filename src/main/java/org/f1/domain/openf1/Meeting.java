package org.f1.domain.openf1;

import java.util.Date;

public record Meeting(int id, String code, Date startDate, String gmtOffset, String location,
                      String name,
                      String officialName, int year, Country country, Circuit circuit) {


    public static final class MeetingBuilder {
        private int id;
        private Date startDate;
        private String gmtOffset;
        private String location;
        private String name;
        private String officialName;
        private int year;
        private Country country;
        private Circuit circuit;
        private String code;

        private MeetingBuilder() {
        }

        public static MeetingBuilder aMeeting() {
            return new MeetingBuilder();
        }

        public MeetingBuilder withId(int id) {
            this.id = id;
            return this;
        }

        public MeetingBuilder withCode(String code) {
            this.code = code;
            return this;
        }

        public MeetingBuilder withStartDate(Date startDate) {
            this.startDate = startDate;
            return this;
        }

        public MeetingBuilder withGmtOffset(String gmtOffset) {
            this.gmtOffset = gmtOffset;
            return this;
        }

        public MeetingBuilder withLocation(String location) {
            this.location = location;
            return this;
        }

        public MeetingBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public MeetingBuilder withOfficialName(String officialName) {
            this.officialName = officialName;
            return this;
        }

        public MeetingBuilder withYear(int year) {
            this.year = year;
            return this;
        }

        public MeetingBuilder withCountry(Country country) {
            this.country = country;
            return this;
        }

        public MeetingBuilder withCircuit(Circuit circuit) {
            this.circuit = circuit;
            return this;
        }

        public Meeting build() {
            return new Meeting(id, code, startDate, gmtOffset, location, name, officialName, year, country, circuit);
        }
    }

}
