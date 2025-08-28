create table f1.drivers
(
    id          int
        constraint drivers_pk
            primary key,
    driverRef   varchar(32),
    number      int,
    code        varchar(3),
    forename    varchar(32),
    surname     varchar(32),
    dob         date,
    nationality varchar(32),
    url         text
);
