package org.f1.domain.openf1;

import java.util.List;

public record SessionResult(String id,
                            int sessionId,
                            int driverNumber,
                            Double duration,
                            Double gapToLeader,
                            int numberOfLaps,
                            int position,
                            boolean dnf,
                            boolean dns,
                            boolean dsq) {


    public static final class SessionResultBuilder {
        private String id;
        private int sessionId;
        private int driverNumber;
        private Double duration;
        private Double gapToLeader;
        private int numberOfLaps;
        private int position;
        private boolean dnf;
        private boolean dns;
        private boolean dsq;

        private SessionResultBuilder() {
        }

        public static SessionResultBuilder aSessionResult() {
            return new SessionResultBuilder();
        }

        public SessionResultBuilder withId(String id) {
            this.id = id;
            return this;
        }

        public SessionResultBuilder withSessionId(int sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        public SessionResultBuilder withDriverNumber(int driverNumber) {
            this.driverNumber = driverNumber;
            return this;
        }

        public SessionResultBuilder withDuration(Double duration) {
            this.duration = duration;
            return this;
        }

        public SessionResultBuilder withGapToLeader(Double gapToLeader) {
            this.gapToLeader = gapToLeader;
            return this;
        }

        public SessionResultBuilder withNumberOfLaps(int numberOfLaps) {
            this.numberOfLaps = numberOfLaps;
            return this;
        }

        public SessionResultBuilder withPosition(int position) {
            this.position = position;
            return this;
        }

        public SessionResultBuilder withDnf(boolean dnf) {
            this.dnf = dnf;
            return this;
        }

        public SessionResultBuilder withDns(boolean dns) {
            this.dns = dns;
            return this;
        }

        public SessionResultBuilder withDsq(boolean dsq) {
            this.dsq = dsq;
            return this;
        }

        public SessionResult build() {
            return new SessionResult(id, sessionId, driverNumber, duration, gapToLeader, numberOfLaps, position, dnf, dns, dsq);
        }
    }

}
