package org.f1.domain.openf1;

import java.util.Date;

public record Session(int id, int meetingId, Date startDate, Date endDate, String sessionName, String sessionType) {


    public static final class SessionBuilder {
        private int id;
        private int meetingId;
        private Date startDate;
        private Date endDate;
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

        public SessionBuilder withStartDate(Date startDate) {
            this.startDate = startDate;
            return this;
        }

        public SessionBuilder withEndDate(Date endDate) {
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
