create table f1.meeting_entity_reference
(
    id         serial
        constraint mert_pk
            primary key,
    driver_id  varchar(50) references f1.driver (id),
    team_id    int references f1.team (id),
    meeting_id int references f1.meeting (id)
);