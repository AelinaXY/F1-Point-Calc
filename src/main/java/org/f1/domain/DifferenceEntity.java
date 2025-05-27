package org.f1.domain;

import java.util.HashSet;
import java.util.Set;

public class DifferenceEntity {

    private Set<BasicPointEntity> out;
    private Set<BasicPointEntity> in;
    private Long numberOfChanges;
    private Double pointDifference;
    private Double threeRacePointDifference;

    public DifferenceEntity() {
        out = new HashSet<>();
        in = new HashSet<>();
        numberOfChanges = 0L;
        pointDifference = 0D;
        threeRacePointDifference = 0D;
    }

    public Set<BasicPointEntity> getOut() {
        return out;
    }

    public void setOut(Set<BasicPointEntity> out) {
        this.out = out;
    }

    public Set<BasicPointEntity> getIn() {
        return in;
    }

    public void setIn(Set<BasicPointEntity> in) {
        this.in = in;
    }

    public Long getNumberOfChanges() {
        return numberOfChanges;
    }

    public void setNumberOfChanges(Long numberOfChanges) {
        this.numberOfChanges = numberOfChanges;
    }

    public Double getPointDifference() {
        return pointDifference;
    }

    public void setPointDifference(Double pointDifference) {
        this.pointDifference = pointDifference;
    }

    public void setThreeRacePointDifference(Double pointDifference) {
        this.threeRacePointDifference = pointDifference;
    }

    public void addOut(Set<BasicPointEntity> basicPointEntity)
    {
        this.out.addAll(basicPointEntity);
    }
    public void addIn(Set<BasicPointEntity> basicPointEntity)
    {
        this.in.addAll(basicPointEntity);
    }

    public void incrementDifference(long count)
    {
        this.numberOfChanges += count;
    }

    @Override
    public String toString() {
        return "DifferenceEntity{" +
                "out=" + out +
                ", in=" + in +
                ", difference=" + numberOfChanges +
                ", pointDifference=" + pointDifference +
                ", threeRacePointDifference=" + threeRacePointDifference +
                '}';
    }
}
