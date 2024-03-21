BEGIN;

DROP TABLE IF EXISTS person CASCADE;
DROP TABLE IF EXISTS job CASCADE;
DROP TABLE IF EXISTS person_with_job CASCADE;
DROP TABLE IF EXISTS language CASCADE;
DROP TABLE IF EXISTS eal_type CASCADE;
DROP TABLE IF EXISTS feature CASCADE;
DROP TABLE IF EXISTS eal CASCADE;
DROP TABLE IF EXISTS eal_with_feature CASCADE;
DROP TABLE IF EXISTS eal_language CASCADE;
DROP TABLE IF EXISTS eal_model CASCADE;
DROP TABLE IF EXISTS toy CASCADE;
DROP TABLE IF EXISTS device CASCADE;
DROP TABLE IF EXISTS eal_status CASCADE;

DROP TYPE IF EXISTS sex CASCADE;

CREATE TYPE sex as ENUM('мужской', 'женский');

CREATE TABLE job(
    id SERIAL PRIMARY KEY,
    name VARCHAR(150) UNIQUE
);

CREATE TABLE person(
    id SERIAL PRIMARY KEY,
    name VARCHAR(30) NOT NULL,
    sex sex NOT NULL,
    date_of_birth DATE NOT NULL
);

CREATE TABLE person_with_job(
    id SERIAL PRIMARY KEY,
    person_id INT NOT NULL REFERENCES person(id),
    job_id INT NOT NULL REFERENCES job(id)
);

CREATE TABLE language(
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    complexity INT CHECK ( complexity >= 1 AND complexity <= 5 ) NOT NULL
);

CREATE TABLE eal_type(
    id SERIAL PRIMARY KEY,
    type VARCHAR(50) UNIQUE NOT NULL
);

CREATE TABLE eal_model(
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    type INT NOT NULL REFERENCES eal_type(id),
    release_date DATE NOT NULL
);

CREATE TABLE eal_status(
    id SERIAL PRIMARY KEY,
    name TEXT UNIQUE NOT NULL
);

CREATE TABLE feature(
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    popularity INT CHECK ( popularity >= 1 AND popularity <=5 ) NOT NULL
);

CREATE TABLE eal(
    id SERIAL PRIMARY KEY,
    model INT NOT NULL REFERENCES eal_model(id),
    version INT CHECK (version >= 1) NOT NULL,
    status INT NOT NULL REFERENCES eal_status(id),
    active_user INT REFERENCES person(id)
);

CREATE TABLE eal_with_feature(
    id SERIAL PRIMARY KEY,
    eal_id INT NOT NULL REFERENCES eal(id),
    feature_id INT NOT NULL REFERENCES feature(id)
);

CREATE TABLE eal_language(
    id SERIAL PRIMARY KEY,
    eal_id INT NOT NULL REFERENCES eal(id),
    language_id INT NOT NULL REFERENCES language(id)
);

CREATE TABLE toy(
    id SERIAL PRIMARY KEY,
    release_date DATE NOT NULL,
    is_speaking BOOLEAN NOT NULL,
    owner INT REFERENCES person(id)
);

CREATE TABLE device(
    id SERIAL PRIMARY KEY,
    device_type VARCHAR(50) NOT NULL,
    is_working BOOLEAN NOT NULL,
    extra_features TEXT,
    eal_id INT NOT NULL REFERENCES eal(id)
);

INSERT INTO job(name)
VALUES ('учёный'),
       ('инженер'),
       ('космонавт'),
       ('капитан'),
       ('научный консультант');

INSERT INTO person (name, sex, date_of_birth)
VALUES ('Флойд', 'мужской', '1955-04-05'),
       ('Александр', 'мужской', '1968-06-10'),
       ('Людмила ', 'женский', '1975-12-25'),
       ('Майкл', 'мужской', '1960-09-18'),
       ('Елена', 'женский', '1982-07-30');

INSERT INTO person_with_job(person_id, job_id)
VALUES (1, 1),
       (2, 2),
       (3, 3),
       (4, 3),
       (5, 5),
       (3, 4);

INSERT INTO language(name, complexity)
VALUES ('английский', 2),
       ('русский', 4),
       ('китайский', 5),
       ('французский', 4),
       ('испанский', 3),
       ('немецкий', 3),
       ('итальянский', 3);

INSERT INTO feature(name, popularity)
VALUES ('разговаривать', 5),
       ('слушать', 5),
       ('перемещаться', 3),
       ('смотреть', 3);

INSERT INTO eal_status(name)
VALUES ('активный'),
       ('отключенный'),
       ('временно недоступный');

INSERT INTO eal_type(type)
VALUES ('голосовой ассистент'),
       ('текстовый ассистент'),
       ('персональный ассистент'),
       ('смешанный тип');

INSERT INTO eal_model(name, type, release_date)
VALUES ('MultiAssist', 4, '2008-11-16'),
       ('VoiceMate', 1, '2005-07-12'),
       ('TextGenius', 2, '2005-10-10'),
       ('PersonalHelper', 3, '2007-04-26');

INSERT INTO eal(model, version, status, active_user)
VALUES (1, 3, 1, 1),
       (2, 2, 1, 3),
       (3, 1, 2, null),
       (4, 5, 3, null);

INSERT INTO eal_with_feature(eal_id, feature_id)
VALUES (1, 1),
       (1, 2),
       (1, 3),
       (2, 1),
       (2, 2),
       (3, 1),
       (4, 1),
       (4, 4);

INSERT INTO eal_language(eal_id, language_id)
VALUES (1, 1),
       (1, 2),
       (2, 1),
       (2, 4),
       (3, 5),
       (4, 2),
       (4, 3);

INSERT INTO toy(release_date, is_speaking, owner)
VALUES ('1955-04-05', TRUE, 1);

INSERT INTO device(device_type, is_working, extra_features, eal_id)
VALUES ('экран', TRUE, NULL, 1),
       ('динамик', TRUE, NULL, 1),
       ('клавиатура', TRUE, NULL, 1),
       ('экран', FALSE, NULL, 2),
       ('экран', TRUE, NULL, 3),
       ('клавиатура', TRUE, NULL, 3);

COMMIT