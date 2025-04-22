package org.f1.enums;

import org.f1.PointEntity;

public enum Teams {

    ALP(new PointEntity("Alpine", 8.1, 1,8.3)),
    AM(new PointEntity("Aston Martin", 8.3, 8.6,12.7)),
    FER(new PointEntity("Ferrari", 27.6, 41.2,64.3)),
    HAA(new PointEntity("Haas", 10, 24.6,20.7)),
    SAU(new PointEntity("Kick Sauber", 4.8, 0.6,-4)),
    MCL(new PointEntity("Mclaren", 31.5, 84.2,83)),
    MER(new PointEntity("Mercedes", 24.2, 59.6, 54)),
    RB(new PointEntity("Racing Bulls", 9.8, 17.2, 18)),
    RBR(new PointEntity("Red Bull Racing", 26.3, 39.8,37.7)),
    WIL(new PointEntity("Williams", 15.3, 19,19.7));

    private final PointEntity pointEntity;

    Teams(PointEntity pointEntity) {
        this.pointEntity = pointEntity;
    }

    public PointEntity getPointEntity() {
        return pointEntity;
    }

}
