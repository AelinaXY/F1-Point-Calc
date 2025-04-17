package org.f1.enums;

import org.f1.PointEntity;

import java.util.Arrays;
import java.util.List;

public enum Drivers {

    ALB(new PointEntity("Alex Albon", 12.8, 12.8)),
    ANT(new PointEntity("Andrea Kimi Antonelli", 19.7, 24.5)),
    SAI(new PointEntity("Carlos Sainz", 10.7, -4)),
    LEC(new PointEntity("Charles Leclerc", 24.7, 10)),
    OCO(new PointEntity("Estaban Ocon", 9.3, 12)),
    ALO(new PointEntity("Fernando Alonso", 6.4, -6.8)),
    BOR(new PointEntity("Gabriel Bortoleto", 4.5, -2.3)),
    RUS(new PointEntity("George Russell", 22, 27.3)),
    HAD(new PointEntity("Isack Hadjar", 5.1, 1)),
    DOO(new PointEntity("Jack Doohan", 5.6, -0.3)),
    STR(new PointEntity("Lance Stroll", 9.7, 10.5)),
    NOR(new PointEntity("Lando Norris", 30, 39.8)),
    HAM(new PointEntity("Lewis Hamilton", 23.2, 13)),
    LAW(new PointEntity("Liam Lawson", 7.2, -3.5)),
    VER(new PointEntity("Max Verstappen", 28.8, 29.3)),
    HUL(new PointEntity("Nico Hulkenberg", 7.2, 0.8)),
    BEA(new PointEntity("Oliver Bearman", 7.9, 12)),
    PIA(new PointEntity("Oscar Piastri", 23.4, 31)),
    GAS(new PointEntity("Pierre Gasly", 9.4, 1.5)),
    TSU(new PointEntity("Yuki Tsunoda", 16.4, 11));

    private final PointEntity pointEntity;

    Drivers(PointEntity pointEntity) {
        this.pointEntity = pointEntity;
    }

    public PointEntity getPointEntity() {
        return pointEntity;
    }

}
