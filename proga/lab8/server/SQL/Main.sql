BEGIN;

CREATE TYPE eyeColor AS ENUM('BLUE', 'YELLOW', 'ORANGE', 'WHITE');
CREATE TYPE nationality AS ENUM('FRANCE', 'THAILAND', 'SOUTH_KOREA');

CREATE TABLE IF NOT EXISTS "user"(
    id SERIAL PRIMARY KEY,
    login VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(64) NOT NULL
);

CREATE TABLE IF NOT EXISTS coordinates(
    id SERIAL PRIMARY KEY,
    x FLOAT,
    y FLOAT NOT NULL
);

CREATE TABLE IF NOT EXISTS location(
    id SERIAL PRIMARY KEY,
    x FLOAT NOT NULL,
    y DOUBLE PRECISION NOT NULL,
    z INTEGER
);

CREATE TABLE IF NOT EXISTS person(
    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL CHECK ( length(name) > 0 ),
    coordinates INT NOT NULL REFERENCES coordinates(id),
    creationDate TIMESTAMP WITH TIME ZONE DEFAULT now(),
    height DOUBLE PRECISION NOT NULL CHECK ( height > 0 ),
    weight DOUBLE PRECISION CHECK ( weight > 0 ),
    eyeColor eyeColor NOT NULL,
    nationality nationality NOT NULL,
    location INT NOT NULL REFERENCES location(id),
    creator INT NOT NULL REFERENCES "user"(id)
);

COMMIT