CREATE TABLE incident (
    id          bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    service     text NOT NULL,
    last_status integer,
    ended_at    timestamptz,
    started_at  timestamptz NOT NULL
)

CREATE UNIQUE INDEX service_incident_open_idx ON service_incident (service) WHERE ended_at IS NULL;