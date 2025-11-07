alter table non_sprint_aggregate_data
    add column if not exists is_sprint float;

alter table non_sprint_aggregate_data
    add column if not exists team_id float;
