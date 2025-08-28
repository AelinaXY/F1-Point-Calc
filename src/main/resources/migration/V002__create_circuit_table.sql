create table f1.circuit
(
    id         int
        constraint circuit_pk
            primary key,
    circuitRef varchar(50),
    name       varchar(50),
    location   varchar(50),
    country    varchar(50),
    lat        float,
    lng        float,
    alt        int,
    url        text
);
