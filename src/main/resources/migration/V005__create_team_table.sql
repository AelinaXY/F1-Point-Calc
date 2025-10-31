create table f1.team
(
    id             serial
        constraint team_pk
            primary key,
    team_name varchar(50)
);