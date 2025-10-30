package org.f1.domain.openf1;

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

    public SessionResult(String id, int sessionId, String driverId, Double duration, Double gapToLeader, int numberOfLaps, int position, boolean dnf, boolean dns, boolean dsq, int driverNumber) {
        this.id = id;
        this.sessionId = sessionId;
        this.driverId = driverId;
        this.duration = duration;
        this.gapToLeader = gapToLeader;
        this.numberOfLaps = numberOfLaps;
        this.position = position;
        this.dnf = dnf;
        this.dns = dns;
        this.dsq = dsq;
        this.driverNumber = driverNumber;
    }

    public String getId() {
        return id;
    }

    public int getSessionId() {
        return sessionId;
    }

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    public Double getDuration() {
        return duration;
    }

    public Double getGapToLeader() {
        return gapToLeader;
    }

    public int getNumberOfLaps() {
        return numberOfLaps;
    }

    public int getPosition() {
        return position;
    }

    public boolean isDnf() {
        return dnf;
    }

    public boolean isDns() {
        return dns;
    }

    public boolean isDsq() {
        return dsq;
    }

    public int getDriverNumber() {
        return driverNumber;
    }

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
