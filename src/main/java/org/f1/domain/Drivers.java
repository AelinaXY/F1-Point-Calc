package org.f1.domain;

public enum Drivers {

    ALB(new BasicPointEntity("Alex Albon", 12.8, 13.3,14.7)),
    ANT(new BasicPointEntity("Andrea Kimi Antonelli", 19.5, 20.7,17.3)),
    SAI(new BasicPointEntity("Carlos Sainz", 9.5, -0.7,5.3)),
    LEC(new BasicPointEntity("Charles Leclerc", 24.3, 9.5,21.7)),
    OCO(new BasicPointEntity("Estaban Ocon", 9.3, 10.2,9.3)),
    ALO(new BasicPointEntity("Fernando Alonso", 5.6, -5.5,5.3)),
    COL(new BasicPointEntity("Franco Colapinto", 4.5, 0.0,0.0)),
    BOR(new BasicPointEntity("Gabriel Bortoleto", 4.5, -2.8,1.7)),
    RUS(new BasicPointEntity("George Russell", 22.2, 25.8,26.3)),
    HAD(new BasicPointEntity("Isack Hadjar", 5.5, 2.0,7.0)),
    STR(new BasicPointEntity("Lance Stroll", 9.7, 11.3,11.3)),
    NOR(new BasicPointEntity("Lando Norris", 30.4, 41.7,41.0)),
    HAM(new BasicPointEntity("Lewis Hamilton", 23.6, 16.3,28.0)),
    LAW(new BasicPointEntity("Liam Lawson", 6.0, -0.5,4.0)),
    VER(new BasicPointEntity("Max Verstappen", 28.8, 27.8,32.0)),
    HUL(new BasicPointEntity("Nico Hulkenberg", 6.0, 2.5,4.3)),
    BEA(new BasicPointEntity("Oliver Bearman", 8.5, 8.2,10.3)),
    PIA(new BasicPointEntity("Oscar Piastri", 24.0, 36.7,47.0)),
    GAS(new BasicPointEntity("Pierre Gasly", 8.2, 1.2,10.3)),
    TSU(new BasicPointEntity("Yuki Tsunoda", 15.2, 9.5,18.3));

    private final BasicPointEntity basicPointEntity;

    Drivers(BasicPointEntity basicPointEntity) {
        this.basicPointEntity = basicPointEntity;
    }

    public BasicPointEntity getPointEntity() {
        return basicPointEntity;
    }

}
