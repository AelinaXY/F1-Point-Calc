alter table meeting_entity_reference
    drop constraint unique_check;

alter table meeting_entity_reference
    add constraint mer_unique_check unique nulls not distinct (driver_id, team_id, meeting_id);