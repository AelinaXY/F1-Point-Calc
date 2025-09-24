create table f1.results
(
    id          int
        constraint results_pk
            primary key,
    raceId int references f1.races (id),
    driverId int references f1.drivers (id),
    constructorId int references f1.constructors (id),
    number int not null,
    grid int,
    position int,
    positionText varchar(32),
    positionOrder int,
    points int,
    laps int,
    time time,
    milliseconds int,
    fastestLap int,
    rank int,
    fastestLapTime time,
    fastestLapSpeed float,
    statusId int references f1.status (id)
);
