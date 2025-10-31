create table f1.driver
(
    id             varchar(50)
        constraint driver_pk
            primary key,
    broadcast_name varchar(50),
    country_code   varchar(4),
    driver_number  int,
    first_name     varchar(50),
    full_name      varchar(50),
    headshot_url   varchar(250),
    last_name      varchar(50),
    name_acronym   varchar(4),
    team_id       int references f1.team (id),
    meeting_id     int references f1.meeting (id)
);