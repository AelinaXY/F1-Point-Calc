create table f1.races
(
    id          int
        constraint races_pk
            primary key,
    year        int references f1.seasons (year),
    round       int,
    circuitId   int references f1.circuit (id),
    name        varchar(50),
    date        date,
    time        time,
    url         text,
    fp1_date    date,
    fp1_time    time,
    fp2_date    date,
    fp2_time    time,
    fp3_date    date,
    fp3_time    time,
    quali_date  date,
    quali_time  time,
    sprint_date date,
    sprint_time time
);
