package org.f1.enums;

import org.f1.PointEntity;

import java.util.Arrays;
import java.util.List;

public enum Teams {

    ALP(new PointEntity("Alpine", 7.9, 2.8)),
    AM(new PointEntity("Aston Martin", 7.7, 8)),
    FER(new PointEntity("Ferrari", 27.3, 35.5)),
    HAA(new PointEntity("Haas", 9.4, 27.8)),
    SAU(new PointEntity("Kick Sauber", 5.4, -1.8)),
    MCL(new PointEntity("Mclaren", 31.2, 83.8)),
    MER(new PointEntity("Mercedes", 23.9, 63)),
    RB(new PointEntity("Racing Bulls", 9.2, 17)),
    RBR(new PointEntity("Red Bull Racing", 26, 44.5)),
    WIL(new PointEntity("Williams", 14.7, 17));

    private final PointEntity pointEntity;

    Teams(PointEntity pointEntity) {
        this.pointEntity = pointEntity;
    }

    public PointEntity getPointEntity() {
        return pointEntity;
    }

}
