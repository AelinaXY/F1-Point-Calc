alter table non_sprint_aggregate_data
    add constraint nsad_mer_unique unique (meeting_entity_reference);