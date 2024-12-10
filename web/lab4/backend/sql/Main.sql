BEGIN;

CREATE TABLE IF NOT EXISTS user_(
    id SERIAL PRIMARY KEY,
    email TEXT UNIQUE NOT NULL,
    password TEXT NOT NULL,
    token TEXT NOT NULL,
    reset_token TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS point(
    id SERIAL PRIMARY KEY,
    x FLOAT NOT NULL,
    y FLOAT NOT NULL,
    r FLOAT NOT NULL,
    result BOOLEAN NOT NULL,
    created_at TEXT NOT NULL,
    execution_time BIGINT NOT NULL,
    created_by INT REFERENCES user_(id) NOT NULL
);

COMMIT