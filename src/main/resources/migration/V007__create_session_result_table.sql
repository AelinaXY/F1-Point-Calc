create table f1.session_result
(
    id             varchar(50)
        constraint session_result_pk
            primary key,
    session_id     int references f1.session (id),
    driver_id  varchar(50) references f1.driver (id),
    duration       float,
    gap_to_leader  float,
    number_of_laps int,
    position       int,
    dnf            boolean,
    dns            boolean,
    dsq            boolean
);