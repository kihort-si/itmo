CREATE SCHEMA orders;

CREATE TABLE orders.order_status(
    ost_id SERIAL PRIMARY KEY,
    code VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE orders.order(
    ord_id SERIAL PRIMARY KEY,
    clnt_id INT NOT NULL,
    order_type VARCHAR(512) NOT NULL,
    ost_id INT NOT NULL,
    date_start TIMESTAMP NOT NULL DEFAULT NOW(),
    date_end TIMESTAMP DEFAULT NULL,
    order_data JSONB NOT NULL,
    FOREIGN KEY (ost_id) REFERENCES orders.order_status(ost_id)
);
