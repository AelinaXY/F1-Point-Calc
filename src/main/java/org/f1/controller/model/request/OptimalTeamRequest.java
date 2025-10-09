package org.f1.controller.model.request;

public record OptimalTeamRequest(
        double costCap,
        long transferLimit,
        String raceName,
        boolean isSprint,
        int racesLeft,
        double costCapMult
) {
}
