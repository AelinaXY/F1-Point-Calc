alter table meeting_entity_reference
    add constraint unique_check unique (driver_id, team_id, meeting_id);