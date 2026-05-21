CREATE SCHEMA depository;

CREATE TABLE depository.portfolio(
    port_id SERIAL PRIMARY KEY,
    clnt_id INTEGER NOT NULL,
    name VARCHAR(512) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    is_closed BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE depository.portfolio_position(
    port_pos_id SERIAL PRIMARY KEY,
    port_id INTEGER NOT NULL,
    ticker VARCHAR(512) NOT NULL,
    amount INTEGER NOT NULL DEFAULT 0,
    amount_frozen INTEGER NOT NULL DEFAULT 0,
    FOREIGN KEY (port_id) REFERENCES depository.portfolio(port_id) ON DELETE CASCADE,
    UNIQUE (port_id, ticker),
    CHECK (amount >= 0),
    CHECK (amount_frozen >= 0)
);

CREATE TABLE depository.operations_type(
    port_oper_type_id SERIAL PRIMARY KEY,
    code VARCHAR(512) NOT NULL
);

CREATE TABLE depository.portfolio_operations_history(
    port_oper_id SERIAL PRIMARY KEY,
    port_id INTEGER NOT NULL,
    ticker VARCHAR(512) NOT NULL,
    port_oper_type_id INTEGER NOT NULL,
    amount INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    FOREIGN KEY (port_id) REFERENCES depository.portfolio(port_id) ON DELETE CASCADE,
    FOREIGN KEY (port_oper_type_id) REFERENCES depository.operations_type(port_oper_type_id) ON DELETE CASCADE
);
