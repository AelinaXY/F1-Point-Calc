package org.f1.enums;

import org.f1.PointEntity;

public enum Teams {

    ALP(new PointEntity("Alpine", 8.3, 2.5,13.7)),
    AM(new PointEntity("Aston Martin", 8.9, 9.3,14.0)),
    FER(new PointEntity("Ferrari", 27.9, 40.0,64.3)),
    HAA(new PointEntity("Haas", 10.6, 21.8,20.7)),
    SAU(new PointEntity("Kick Sauber", 4.5, 2.0,6.3)),
    MCL(new PointEntity("Mclaren", 31.8, 89.8,98.7)),
    MER(new PointEntity("Mercedes", 24.5, 58.2, 55.7)),
    RB(new PointEntity("Racing Bulls", 10.0, 13.3, 18.0)),
    RBR(new PointEntity("Red Bull Racing", 26.6, 43.2,50.7)),
    WIL(new PointEntity("Williams", 15.9, 21.0,25.7));

    private final PointEntity pointEntity;

    Teams(PointEntity pointEntity) {
        this.pointEntity = pointEntity;
    }

    public PointEntity getPointEntity() {
        return pointEntity;
    }

}
