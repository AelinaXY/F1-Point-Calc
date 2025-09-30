package org.f1.calculations;

import org.f1.domain.FullPointEntity;

import java.util.Set;

public abstract class AbstractCalculation {
    private final Set<FullPointEntity> driverSet;
    private final Set<FullPointEntity> teamSet;


    public AbstractCalculation(Set<FullPointEntity> driverSet, Set<FullPointEntity> teamSet) {
        this.driverSet = driverSet;
        this.teamSet = teamSet;
    }

    public Set<FullPointEntity> getDriverSet() {
        return driverSet;
    }

    public Set<FullPointEntity> getTeamSet() {
        return teamSet;
    }

    public void setCostCap(double costCap) {
    }

    public void setSprint(boolean sprint) {
    }
}
