CREATE SCHEMA balm;

CREATE TABLE balm.currency_code(
    curr_id SERIAL PRIMARY KEY,
    code VARCHAR(3) NOT NULL UNIQUE,
    refs_id INT NOT NULL UNIQUE
);

CREATE TABLE balm.account_state(
    accst_id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE balm.account(
    acc_id SERIAL PRIMARY KEY,
    clnt_id INT NOT NULL,
    curr_id INT NOT NULL,
    accst_id INT NOT NULL,
    FOREIGN KEY (curr_id) REFERENCES balm.currency_code(curr_id),
    FOREIGN KEY (accst_id) REFERENCES balm.account_state(accst_id)
);

CREATE TABLE balm.bill_details(
    bdet_id SERIAL PRIMARY KEY,
    code VARCHAR(255) NOT NULL UNIQUE,
    bdet_refs_id INT NOT NULL UNIQUE
);

CREATE TABLE balm.charge_types(
    chtp_id SERIAL PRIMARY KEY,
    code VARCHAR(255) NOT NULL UNIQUE,
    default_bdet_id INT NOT NULL,
    FOREIGN KEY (default_bdet_id) REFERENCES balm.bill_details(bdet_id)
);

CREATE TABLE balm.charge(
    chtg_id SERIAL PRIMARY KEY,
    acc_id INT NOT NULL,
    chtp_id INT NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    bdet_id INT,
    timestamp TIMESTAMP NOT NULL DEFAULT NOW(),
    FOREIGN KEY (acc_id) REFERENCES balm.account(acc_id),
    FOREIGN KEY (chtp_id) REFERENCES balm.charge_types(chtp_id),
    FOREIGN KEY (bdet_id) REFERENCES balm.bill_details(bdet_id)
);

CREATE TABLE balm.balance_cache(
    bacc_id SERIAL PRIMARY KEY,
    acc_id INT NOT NULL,
    balance DECIMAL(15, 2) NOT NULL,
    FOREIGN KEY (acc_id) REFERENCES balm.account(acc_id),
    CHECK (balance >= 0)
);

-- CREATE TABLE balm.purchase_order_type(
--     potp_id SERIAL PRIMARY KEY,
--     code VARCHAR(255) NOT NULL UNIQUE
-- );

-- CREATE TABLE balm.purchase_order_status(
--     post_id SERIAL PRIMARY KEY,
--     code VARCHAR(255) NOT NULL UNIQUE
-- );

-- CREATE TABLE balm.purchase_order(
--     pord_id SERIAL PRIMARY KEY,
--     acc_id INT NOT NULL,
--     potp_id INT NOT NULL,
--     post_id INT NOT NULL,
--     date_start TIMESTAMP NOT NULL DEFAULT NOW(),
--     date_end TIMESTAMP DEFAULT NULL,
--     order_data JSONB NOT NULL,
--     FOREIGN KEY (acc_id) REFERENCES balm.account(acc_id),
--     FOREIGN KEY (potp_id) REFERENCES balm.purchase_order_type(potp_id),
--     FOREIGN KEY (post_id) REFERENCES balm.purchase_order_status(post_id)
-- );

CREATE TABLE balm.scheme_type(
    scht_id SERIAL PRIMARY KEY,
    code varchar(255) UNIQUE NOT NULL 
);

CREATE TABLE balm.scheme_script(
    schem_id SERIAL PRIMARY KEY,
    scht_id INT NOT NULL,
    code VARCHAR(255) NOT NULL UNIQUE,
    script TEXT NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    FOREIGN KEY (scht_id) REFERENCES balm.scheme_type(scht_id)
);

CREATE TABLE balm.fees(
    fee_id SERIAL PRIMARY KEY,
    region_id INT NOT NULL,
    code VARCHAR(255) NOT NULL,
    amount DECIMAL(5, 2) NOT NULL,
    UNIQUE (code, region_id)
);

CREATE TABLE balm.client_attribute(
    clnt_attr_id SERIAL PRIMARY KEY,
    clnt_id INTEGER NOT NULL,
    attribute_refs_id INTEGER NOT NULL,
    start_date TIMESTAMP NOT NULL DEFAULT NOW(),
    end_date TIMESTAMP,
    value VARCHAR(512) NOT NULL,
    UNIQUE (clnt_id, attribute_refs_id)
);

CREATE TABLE balm.scheduled_report(
    sched_rep_id SERIAL PRIMARY KEY,
    clnt_id INTEGER NOT NULL,
    report_type VARCHAR(255) NOT NULL,
    schedule JSONB NOT NULL,
    start_date TIMESTAMP NOT NULL DEFAULT NOW(),
    end_date TIMESTAMP,
    UNIQUE (clnt_id, report_type)
);