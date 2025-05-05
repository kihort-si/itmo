BEGIN;

CREATE TABLE points
(
    id      SERIAL PRIMARY KEY,
    x       FLOAT   NOT NULL,
    y       FLOAT   NOT NULL,
    r       FLOAT   NOT NULL,
    result  BOOLEAN NOT NULL,
    date    DATE    NOT NULL,
    session VARCHAR(255)
);

COMMIT;