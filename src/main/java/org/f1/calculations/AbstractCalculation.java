package org.f1.calculations;

import org.f1.domain.FullPointEntity;

import java.util.Set;

public abstract class AbstractCalculation {
    private final Set<FullPointEntity> driverSet;
    private final Set<FullPointEntity> teamSet;
    private final double costCap;
    private final long transferLimit;
    private final String raceName;


    public AbstractCalculation(Set<FullPointEntity> driverSet, Set<FullPointEntity> teamSet, double costCap, long transferLimit, String raceName) {
        this.driverSet = driverSet;
        this.teamSet = teamSet;
        this.costCap = costCap;
        this.transferLimit = transferLimit;
        this.raceName = raceName;
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
}
