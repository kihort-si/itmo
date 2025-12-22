INSERT INTO person (name, passportID, eyeColor, hairColor, nationality, location_x, location_y, location_z, location_name) VALUES
                                                                                                                               ('Alice Smith', 'AA1234567', 'BLUE', 'GREEN', 'UNITED_KINGDOM', 10, 20.5, 30.7, 'New York'),
                                                                                                                               ('Bob Johnson', 'BB2345678', 'GREEN', 'BLUE', 'THAILAND', -15, 40.2, -5.3, 'Toronto'),
                                                                                                                               ('Charlie Lee', 'CC3456789', 'BLUE', null, 'THAILAND', 0, 0.0, 0.0, 'Beijing'),
                                                                                                                               ('Diana King', 'DD4567890', 'GREEN', 'BLUE', 'UNITED_KINGDOM', 100, 10.1, 10.2, 'London'),
                                                                                                                               ('Ethan White', 'EE5678901', 'YELLOW', null, 'UNITED_KINGDOM', -50, -20.0, 5.5, 'Berlin'),
                                                                                                                               ('Fiona Green', 'FF6789012', 'BLUE', 'YELLOW', 'UNITED_KINGDOM', 25, 33.3, 44.4, 'Paris'),
                                                                                                                               ('George Black', 'GG7890123', 'GREEN', 'GREEN', 'GERMANY', 77, 22.2, 11.1, 'Rome'),
                                                                                                                               ('Hannah Blue', 'HH8901234', null, null, 'UNITED_KINGDOM', -33, 55.5, -44.4, 'Madrid'),
                                                                                                                               ('Ian Gold', 'II9012345', 'YELLOW', 'BLUE', 'GERMANY', 11, 66.6, 77.7, 'Sydney'),
                                                                                                                               ('Julia Red', 'JJ0123456', 'BLUE', null, 'GERMANY', 88, -11.1, 99.9, 'Tokyo');


INSERT INTO movie (name, oscarsCount, budget, totalBoxOffice, length, goldenPalmCount, mpaaRating, genre, coordinates_x, coordinates_y, director_id, screenwriter_id, operator_id) VALUES
                                                                                                                                                                                       ('The Great Adventure', 3, 50000000, 150000000.0, 120, 1, 'PG_13', 'ADVENTURE', 45.6, 100, 23, NULL, NULL),
                                                                                                                                                                                       ('Romantic Days', 1, 20000000, 60000000.0, 90, 2, 'PG', 'COMEDY', 30.2, 80, 24, NULL, NULL),
                                                                                                                                                                                       ('The Mystery House', 0, 15000000, 45000000.0, 110, 1, 'R', 'HORROR', 20.3, 120, 22, NULL, NULL),
                                                                                                                                                                                       ('Future World', 5, 80000000, 300000000.0, 140, 3, 'PG_13', 'TRAGEDY', 60.1, 130, 29, NULL, NULL),
                                                                                                                                                                                       ('Under the Sky', 2, 35000000, 95000000.0, 100, 1, 'PG', 'TRAGEDY', -15.5, 50, 22, NULL, NULL),
                                                                                                                                                                                       ('Battlefield Earth', 4, 100000000, 400000000.0, 150, 2, 'R', 'COMEDY', -50.0, 200, 25, NULL, NULL),
                                                                                                                                                                                       ('Deep Ocean', 1, 30000000, 120000000.0, 95, 1, 'PG', 'TRAGEDY', 0.0, 90, 28, NULL, NULL),
                                                                                                                                                                                       ('Space Journey', 3, 60000000, 220000000.0, 130, 3, 'PG_13', 'TRAGEDY', 70.7, 110, 30, NULL, NULL),
                                                                                                                                                                                       ('The Silent Voice', 2, 25000000, 80000000.0, 85, 1, 'PG', 'ADVENTURE', 33.3, 40, 23, NULL, NULL),
                                                                                                                                                                                       ('Winds of Change', 0, 10000000, 30000000.0, 75, 1, 'G', null, 22.2, 60, 26, NULL, NULL),
                                                                                                                                                                                       ('Forgotten Legends', 4, 70000000, 210000000.0, 125, 3, 'PG_13', 'HORROR', 90.9, 100, 28, NULL, NULL),
                                                                                                                                                                                       ('Rising Sun', 1, 12000000, 45000000.0, 100, 1, 'PG', 'ADVENTURE', 44.4, 30, 23, NULL, NULL),
                                                                                                                                                                                       ('Last Horizon', 3, 40000000, 170000000.0, 135, 2, 'R', 'HORROR', 55.5, 200, 24, NULL, NULL),
                                                                                                                                                                                       ('City Lights', 0, 5000000, 25000000.0, 80, 1, 'G', 'COMEDY', 11.1, 70, 27, NULL, NULL),
                                                                                                                                                                                       ('Ocean Breath', 2, 28000000, 110000000.0, 90, 2, 'PG', 'ADVENTURE', -22.2, 120, 29, NULL, NULL);

ALTER TABLE movie
    ADD CONSTRAINT check_movie_budget_positive CHECK (budget > 0);

ALTER TABLE movie
    ADD CONSTRAINT check_coordinates_y_max CHECK (coordinates_y <= 347);

ALTER TABLE movie
    ADD CONSTRAINT check_oscars_count_non_negative CHECK (oscarsCount >= 0);

ALTER TABLE movie
    ADD CONSTRAINT check_goldenPalm_count_non_negative CHECK (goldenpalmcount >= 0);

ALTER TABLE movie
    ADD CONSTRAINT check_movie_boxes_positive CHECK (totalboxoffice > 0);

ALTER TABLE movie
    ADD CONSTRAINT check_movie_length_positive CHECK (length > 0);


CREATE TABLE file(
                     id SERIAL PRIMARY KEY,
                     filename VARCHAR(255) NOT NULL,
                     size BIGINT NOT NULL,
                     creationdate TIMESTAMP DEFAULT now(),
                     success BOOLEAN NOT NULL
);