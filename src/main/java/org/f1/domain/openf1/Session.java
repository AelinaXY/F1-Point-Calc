package org.f1.domain.openf1;

import java.time.OffsetDateTime;

public record Session(int id, int meetingId, OffsetDateTime startDate, OffsetDateTime endDate, String sessionName, String sessionType) {


    public static final class SessionBuilder {
        private int id;
        private int meetingId;
        private OffsetDateTime startDate;
        private OffsetDateTime endDate;
        private String sessionName;
        private String sessionType;

        private SessionBuilder() {
        }

        public static SessionBuilder aSession() {
            return new SessionBuilder();
        }

        public SessionBuilder withId(int id) {
            this.id = id;
            return this;
        }

        public SessionBuilder withMeetingId(int meetingId) {
            this.meetingId = meetingId;
            return this;
        }

        public SessionBuilder withStartDate(OffsetDateTime startDate) {
            this.startDate = startDate;
            return this;
        }

        public SessionBuilder withEndDate(OffsetDateTime endDate) {
            this.endDate = endDate;
            return this;
        }

        public SessionBuilder withSessionName(String sessionName) {
            this.sessionName = sessionName;
            return this;
        }

        public SessionBuilder withSessionType(String sessionType) {
            this.sessionType = sessionType;
            return this;
        }

        public Session build() {
            return new Session(this.id, this.meetingId, this.startDate, this.endDate, this.sessionName, this.sessionType);
        }
    }

}
