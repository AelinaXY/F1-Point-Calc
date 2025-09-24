create table f1.qualifying
(
    id          int
        constraint qualifying_pk
            primary key,
    raceId int references f1.races (id),
    driverId int references f1.drivers (id),
    constructorId int references f1.constructors (id),
    number int not null,
    position int,
    q1 time,
    q2 time,
    q3 time
);
