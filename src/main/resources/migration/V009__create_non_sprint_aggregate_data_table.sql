create table f1.non_sprint_aggregate_data
(
    id                       serial
        constraint nsad_pk
            primary key,
--     IDENTIFIER
    meeting_entity_reference int references f1.meeting_entity_reference (id),
--     LABEL
    actual_points            int,
--     DATA
    avg_points               float,
    avg_4d1_points           float,
    stdev                    float,
    is_team                  float
);