package org.f1.domain;

import java.util.HashMap;
import java.util.Map;

public class Track {
    String trackName;
    Double avgTemp;
    Double avgRain;
    Double trackLength;
    Double pitLaneTimeLoss;
    Double fastestLap;
    Double topSpeed;
    Double numberOfCorners;
    Double avgOvertakes;
    Map<String, Double> distanceToOtherTrack;

    public Track(String trackName, Double avgTemp, Double avgRain, Double trackLength, Double pitLaneTimeLoss, Double fastestLap, Double topSpeed, Double numberOfCorners, Double avgOvertakes) {
        this.trackName = trackName;
        this.avgTemp = avgTemp;
        this.avgRain = avgRain;
        this.trackLength = trackLength;
        this.pitLaneTimeLoss = pitLaneTimeLoss;
        this.fastestLap = fastestLap;
        this.topSpeed = topSpeed;
        this.numberOfCorners = numberOfCorners;
        this.avgOvertakes = avgOvertakes;
        this.distanceToOtherTrack = new HashMap<>();
    }

    public void addTrackRelationship(String otherTrackName, Double distance) {
        this.distanceToOtherTrack.put(otherTrackName, distance);
    }

    public String getTrackName() {
        return trackName;
    }

    public Double getAvgTemp() {
        return avgTemp;
    }

    public Double getAvgRain() {
        return avgRain;
    }

    public Double getTrackLength() {
        return trackLength;
    }

    public Double getPitLaneTimeLoss() {
        return pitLaneTimeLoss;
    }

    public Double getFastestLap() {
        return fastestLap;
    }

    public Double getTopSpeed() {
        return topSpeed;
    }

    public Double getNumberOfCorners() {
        return numberOfCorners;
    }

    public Double getAvgOvertakes() {
        return avgOvertakes;
    }

    public Map<String, Double> getDistanceToOtherTrack() {
        return distanceToOtherTrack;
    }

    public static class TrackBuilder {
        String trackName;
        Double avgTemp;
        Double avgRain;
        Double trackLength;
        Double pitLaneTimeLoss;
        Double fastestLap;
        Double topSpeed;
        Double numberOfCorners;
        Double avgOvertakes;

        public static TrackBuilder aTrackBuilder() {
            return new TrackBuilder();
        }

        public TrackBuilder withTrackName(String trackName) {
            this.trackName = trackName;
            return this;
        }

        public TrackBuilder withAvgTemp(Double avgTemp) {
            this.avgTemp = avgTemp;
            return this;
        }

        public TrackBuilder withAvgRain(Double avgRain) {
            this.avgRain = avgRain;
            return this;
        }

        public TrackBuilder withTrackLength(Double trackLength) {
            this.trackLength = trackLength;
            return this;
        }

        public TrackBuilder withPitLaneTimeLoss(Double pitLaneTimeLoss) {
            this.pitLaneTimeLoss = pitLaneTimeLoss;
            return this;
        }

        public TrackBuilder withFastestLap(Double fastestLap) {
            this.fastestLap = fastestLap;
            return this;
        }

        public TrackBuilder withTopSpeed(Double topSpeed) {
            this.topSpeed = topSpeed;
            return this;
        }

        public TrackBuilder withNumberOfCorners(Double numberOfCorners) {
            this.numberOfCorners = numberOfCorners;
            return this;
        }

        public TrackBuilder withAvgOvertakes(Double avgOvertakes) {
            this.avgOvertakes = avgOvertakes;
            return this;
        }

        public Track build() {
            return new Track(trackName, avgTemp, avgRain, trackLength, pitLaneTimeLoss, fastestLap, topSpeed, numberOfCorners, avgOvertakes);
        }


    }

}
