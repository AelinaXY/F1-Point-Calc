create type tyre_compound as enum ('SOFT', 'MEDIUM', 'HARD', 'INTERMEDIATE', 'WET');
create table f1.stint
(
    stint_id varchar(50),
    session_key int references f1.session (id),
    driver_id varchar(50) references f1.driver(id),
    stint_number int,
    lap_start int,
    compound tyre_compound,
    lap_end           int,
    tyre_age_at_start int,
    primary key (stint_id),
    constraint unique_stint unique (session_key, driver_id, stint_number)
);
