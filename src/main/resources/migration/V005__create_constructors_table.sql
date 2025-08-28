create table f1.constructors
(
    id             int
        constraint constructors_pk
            primary key,
    constructorRef varchar(32),
    name           varchar(32),
    nationality    varchar(32),
    url            text
);
