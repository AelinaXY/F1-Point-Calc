package org.f1.calculations;

import org.f1.domain.FullPointEntity;

import java.util.Set;

public abstract class AbstractCalculation {
    private final Set<FullPointEntity> driverSet;
    private final Set<FullPointEntity> teamSet;
    private double costCap;
    private long transferLimit;
    private String raceName;
    private boolean isSprint;


    public AbstractCalculation(Set<FullPointEntity> driverSet, Set<FullPointEntity> teamSet, double costCap, long transferLimit, String raceName, boolean isSprint) {
        this.driverSet = driverSet;
        this.teamSet = teamSet;
        this.costCap = costCap;
        this.transferLimit = transferLimit;
        this.raceName = raceName;
        this.isSprint = isSprint;
    }

    public Set<FullPointEntity> getDriverSet() {
        return driverSet;
    }

    public Set<FullPointEntity> getTeamSet() {
        return teamSet;
    }

    public double getCostCap() {
        return costCap;
    }

    public long getTransferLimit() {
        return transferLimit;
    }

    public String getRaceName() {
        return raceName;
    }

    public boolean isSprint() {
        return isSprint;
    }

    public void setCostCap(double costCap) {
        this.costCap = costCap;
    }

    public void setTransferLimit(long transferLimit) {
        this.transferLimit = transferLimit;
    }

    public void setRaceName(String raceName) {
        this.raceName = raceName;
    }

    public void setSprint(boolean sprint) {
        isSprint = sprint;
    }
}
