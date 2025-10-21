package org.f1.domain.openf1;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class SessionResult {
    private String id;
    private int sessionId;
    private String driverId;
    private Double duration;
    private Double gapToLeader;
    private int numberOfLaps;
    private int position;
    private boolean dnf;
    private boolean dns;
    private boolean dsq;
    private int driverNumber;


    public static final class SessionResultBuilder {
        private String id;
        private int sessionId;
        private String driverId;
        private Double duration;
        private Double gapToLeader;
        private int numberOfLaps;
        private int position;
        private boolean dnf;
        private boolean dns;
        private boolean dsq;
        private int driverNumber;

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

        public SessionResultBuilder withDriverId(String driverId) {
            this.driverId = driverId;
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

        public SessionResultBuilder withDriverNumber(int driverNumber) {
            this.driverNumber = driverNumber;
            return this;
        }

        public SessionResult build() {
            return new SessionResult(id, sessionId, driverId, duration, gapToLeader, numberOfLaps, position, dnf, dns, dsq, driverNumber);
        }
    }

}
