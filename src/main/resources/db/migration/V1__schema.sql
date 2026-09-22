CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE telemetry_sample (
    sampled_at       timestamptz PRIMARY KEY,
    cpu_percent      real   NOT NULL,
    mem_used_bytes   bigint NOT NULL,
    mem_total_bytes  bigint NOT NULL,
    disk_used_bytes  bigint NOT NULL,
    disk_total_bytes bigint NOT NULL,
    load_1m          real   NOT NULL,
    uptime_seconds   bigint NOT NULL,
    temp_celsius     real
);

CREATE TABLE service_check (
    up          boolean     NOT NULL,
    service     text        NOT NULL,
    checked_at  timestamptz NOT NULL,
    latency_ms  integer,
    status_code integer,
    PRIMARY KEY (service, checked_at)
);

CREATE TABLE service_day (
    service     text    NOT NULL,
    day         date    NOT NULL,
    checks      integer NOT NULL,
    ups         integer NOT NULL,
    latency_sum bigint  NOT NULL,
    PRIMARY KEY (service, day)
);

CREATE TABLE vector_store (
    id        uuid PRIMARY KEY,
    content   text,
    metadata  json,
    embedding vector(1024)
);

CREATE INDEX ON vector_store USING hnsw (embedding vector_cosine_ops);