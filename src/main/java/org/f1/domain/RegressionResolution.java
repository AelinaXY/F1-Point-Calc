package org.f1.domain;

public class RegressionResolution {
    private Double bounds;
    private Double interval;

    public RegressionResolution(Double bounds, Double interval) {
        this.bounds = bounds;
        this.interval = interval;
    }

    public Double getEdgeBounds() {
        return bounds/2;
    }

    public void setBounds(Double bounds) {
        this.bounds = bounds;
    }

    public Double getInterval() {
        return interval;
    }

    public void setInterval(Double interval) {
        this.interval = interval;
    }

    public void lowerResolution() {
        bounds = bounds/5;
        interval = interval/5;
    }
}
