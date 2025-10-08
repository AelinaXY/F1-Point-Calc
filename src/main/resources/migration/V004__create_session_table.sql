create table f1.session
(
    id           int
        constraint session_pk
            primary key,
    meeting_id   int references f1.meeting (id),
    date_start   timestamp,
    date_end     timestamp,
    session_name varchar(50),
    session_type varchar(50)
);