package org.f1.domain;

import java.util.HashSet;
import java.util.Set;

public class DifferenceEntity {

    private Set<BasicPointEntity> out;
    private Set<BasicPointEntity> in;
    private Long numberOfChanges;
    private Double scoreDifference;

    public DifferenceEntity() {
        out = new HashSet<>();
        in = new HashSet<>();
        numberOfChanges = 0L;
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

    public void setScoreDifference(Double scoreDifference) {
        this.scoreDifference = scoreDifference;
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
                ", scoreDifference=" + scoreDifference +
                '}';
    }
}
