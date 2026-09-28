CREATE TABLE incident (
    id          bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    service     text NOT NULL,
    last_status integer,
    ended_at    timestamptz,
    started_at  timestamptz NOT NULL
);

CREATE UNIQUE INDEX incident_open_idx ON incident (service) WHERE ended_at IS NULL;
