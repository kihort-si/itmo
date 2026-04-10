INSERT INTO lab_test (data)
VALUES ('step4 new row A'),
       ('step4 new row B'),
       ('step4 new row C');

SELECT *
FROM lab_test
ORDER BY id;
SELECT now() AS control_time;
SELECT pg_switch_wal();

DELETE
FROM lab_test
WHERE mod(id, 2) = 0;

SELECT *
FROM lab_test
ORDER BY id;

SELECT pg_switch_wal();

SELECT *
FROM lab_test
ORDER BY id;

CREATE TABLE lab_test_restore
(
    LIKE lab_test INCLUDING ALL
);
INSERT INTO lab_test
SELECT *
FROM lab_test_restore ON CONFLICT (id) DO NOTHING;

SELECT * FROM lab_test ORDER BY id;

DROP TABLE lab_test_restore;