create table f1.meeting
(
    id            int
        constraint meeting_pk
            primary key,
    circuit_id    int references f1.circuit (id),
    country_id    int references f1.country (id),
    date_start    timestamp,
    gmt_offset    interval,
    location      varchar(50),
    name          varchar(50),
    official_name varchar(200),
    year          int
);
