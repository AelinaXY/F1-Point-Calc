package org.f1.domain;

public enum Teams {

    ALP(new BasicPointEntity("Alpine", 8.3, 2.5,13.7)),
    AM(new BasicPointEntity("Aston Martin", 8.9, 9.3,14.0)),
    FER(new BasicPointEntity("Ferrari", 27.9, 40.0,64.3)),
    HAA(new BasicPointEntity("Haas", 10.6, 21.8,20.7)),
    SAU(new BasicPointEntity("Kick Sauber", 4.5, 2.0,6.3)),
    MCL(new BasicPointEntity("Mclaren", 31.8, 89.8,98.7)),
    MER(new BasicPointEntity("Mercedes", 24.5, 58.2, 55.7)),
    RB(new BasicPointEntity("Racing Bulls", 10.0, 13.3, 18.0)),
    RBR(new BasicPointEntity("Red Bull Racing", 26.6, 43.2,50.7)),
    WIL(new BasicPointEntity("Williams", 15.9, 21.0,25.7));

    private final BasicPointEntity basicPointEntity;

    Teams(BasicPointEntity basicPointEntity) {
        this.basicPointEntity = basicPointEntity;
    }

    public BasicPointEntity getPointEntity() {
        return basicPointEntity;
    }

}
