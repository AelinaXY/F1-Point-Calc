package org.f1.enums;

import org.f1.PointEntity;

public enum Drivers {

    ALB(new PointEntity("Alex Albon", 12.6, 12.2,11)),
    ANT(new PointEntity("Andrea Kimi Antonelli", 19.6, 22.6,17.3)),
    SAI(new PointEntity("Carlos Sainz", 10.1, -0.8,2.3)),
    LEC(new PointEntity("Charles Leclerc", 24.6, 13.0,21.7)),
    OCO(new PointEntity("Estaban Ocon", 9.1, 10.8,7.3)),
    ALO(new PointEntity("Fernando Alonso", 6.2, -4,5.3)),
    BOR(new PointEntity("Gabriel Bortoleto", 4.5, -1.2,1.7)),
    RUS(new PointEntity("George Russell", 22.1, 25,21.7)),
    HAD(new PointEntity("Isack Hadjar", 5.7, 1.8,6.0)),
    DOO(new PointEntity("Jack Doohan", 5, 0,3.0)),
    STR(new PointEntity("Lance Stroll", 9.1, 8.6,3.3)),
    NOR(new PointEntity("Lando Norris", 30.1, 39.0,31.7)),
    HAM(new PointEntity("Lewis Hamilton", 23.3, 13.2,21)),
    LAW(new PointEntity("Liam Lawson", 6.6, 4,4)),
    VER(new PointEntity("Max Verstappen", 28.9, 31,32)),
    HUL(new PointEntity("Nico Hulkenberg", 6.6, 1.2,-5.3)),
    BEA(new PointEntity("Oliver Bearman", 8.5, 10.6,10.3)),
    PIA(new PointEntity("Oscar Piastri", 23.7, 32.4,35.7)),
    GAS(new PointEntity("Pierre Gasly", 8.8, -2.2,-1.3)),
    TSU(new PointEntity("Yuki Tsunoda", 15.8, 1.7,1.7));

    private final PointEntity pointEntity;

    Drivers(PointEntity pointEntity) {
        this.pointEntity = pointEntity;
    }

    public PointEntity getPointEntity() {
        return pointEntity;
    }

}
