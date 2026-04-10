CREATE TABLE lab_test (
id SERIAL PRIMARY KEY,
data TEXT,
created_at TIMESTAMP DEFAULT now()
);
INSERT INTO lab_test (data) VALUES
('test row 1'), ('test row 2'), ('test row 3'),
('test row 4'), ('test row 5');

SELECT * FROM lab_test ORDER BY id;

SELECT pg_switch_wal();

SELECT pg_is_in_recovery();

SELECT * FROM lab_test ORDER BY id;