package org.f1.enums;

import org.f1.domain.PointEntity;

public enum Drivers {

    ALB(new PointEntity("Alex Albon", 12.8, 13.3,14.7)),
    ANT(new PointEntity("Andrea Kimi Antonelli", 19.5, 20.7,17.3)),
    SAI(new PointEntity("Carlos Sainz", 9.5, -0.7,5.3)),
    LEC(new PointEntity("Charles Leclerc", 24.3, 9.5,21.7)),
    OCO(new PointEntity("Estaban Ocon", 9.3, 10.2,9.3)),
    ALO(new PointEntity("Fernando Alonso", 5.6, -5.5,5.3)),
    COL(new PointEntity("Franco Colapinto", 4.5, 0.0,0.0)),
    BOR(new PointEntity("Gabriel Bortoleto", 4.5, -2.8,1.7)),
    RUS(new PointEntity("George Russell", 22.2, 25.8,26.3)),
    HAD(new PointEntity("Isack Hadjar", 5.5, 2.0,7.0)),
    STR(new PointEntity("Lance Stroll", 9.7, 11.3,11.3)),
    NOR(new PointEntity("Lando Norris", 30.4, 41.7,41.0)),
    HAM(new PointEntity("Lewis Hamilton", 23.6, 16.3,28.0)),
    LAW(new PointEntity("Liam Lawson", 6.0, -0.5,4.0)),
    VER(new PointEntity("Max Verstappen", 28.8, 27.8,32.0)),
    HUL(new PointEntity("Nico Hulkenberg", 6.0, 2.5,4.3)),
    BEA(new PointEntity("Oliver Bearman", 8.5, 8.2,10.3)),
    PIA(new PointEntity("Oscar Piastri", 24.0, 36.7,47.0)),
    GAS(new PointEntity("Pierre Gasly", 8.2, 1.2,10.3)),
    TSU(new PointEntity("Yuki Tsunoda", 15.2, 9.5,18.3));

    private final PointEntity pointEntity;

    Drivers(PointEntity pointEntity) {
        this.pointEntity = pointEntity;
    }

    public PointEntity getPointEntity() {
        return pointEntity;
    }

}
