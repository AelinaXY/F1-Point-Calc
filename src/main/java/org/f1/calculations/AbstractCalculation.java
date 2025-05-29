package org.f1.calculations;

import org.f1.domain.FullPointEntity;

import java.util.Set;

public abstract class AbstractCalculation {
    private final Set<FullPointEntity> driverSet;
    private final Set<FullPointEntity> teamSet;
    private final double costCap;
    private final long transferLimit;


    protected AbstractCalculation(Set<FullPointEntity> driverSet, Set<FullPointEntity> teamSet, double costCap, long transferLimit) {
        this.driverSet = driverSet;
        this.teamSet = teamSet;
        this.costCap = costCap;
        this.transferLimit = transferLimit;
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
}
