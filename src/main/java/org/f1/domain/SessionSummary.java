package org.f1.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SessionSummary {
    private static final int MISSING_POSITION = 23;
    private static final double MISSING_GAP = -1d;
    private static final double MISSING_LAPS_DONE = -1d;

    private boolean available;
    private int position;
    private double gap;
    private double lapsDone;

    public SessionSummary(boolean available, Integer position, Double gap, Double lapsDone) {
        this.available = available;
        this.position = nullCheck(position, MISSING_POSITION);
        this.gap = nullCheck(gap, MISSING_GAP);
        this.lapsDone = nullCheck(lapsDone, MISSING_LAPS_DONE);
    }

    private static <T> T nullCheck(T value, T defaultValue) {
        return value == null ? defaultValue : value;
    }
}
