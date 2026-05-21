CREATE SCHEMA clients;

CREATE TABLE clients.client_status(
    clst_id SERIAL PRIMARY KEY,
    def VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE clients.client(
    clnt_id SERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    region_refs_identifier INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    language_code VARCHAR(10) NOT NULL,
    status_id INTEGER NOT NULL,
    FOREIGN KEY (status_id) REFERENCES clients.client_status(clst_id)
);

CREATE TABLE clients.client_account(
    clnt_acc_id SERIAL PRIMARY KEY,
    clnt_id INTEGER NOT NULL,
    account_id INTEGER NOT NULL UNIQUE,
    FOREIGN KEY (clnt_id) REFERENCES clients.client(clnt_id)
);

CREATE TABLE clients.client_attribute(
    clnt_attr_id SERIAL PRIMARY KEY,
    clnt_id INTEGER NOT NULL,
    attribute_refs_id INTEGER NOT NULL,
    start_date TIMESTAMP NOT NULL DEFAULT NOW(),
    end_date TIMESTAMP,
    value VARCHAR(512) NOT NULL,
    FOREIGN KEY (clnt_id) REFERENCES clients.client(clnt_id),
    UNIQUE (clnt_id, attribute_refs_id)
);

CREATE TABLE clients.client_details(
    clnt_details_id SERIAL PRIMARY KEY,
    clnt_id INTEGER NOT NULL,
    full_name VARCHAR(512) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone_number VARCHAR(20),
    additional_info TEXT,
    profile_extension JSONB,
    FOREIGN KEY (clnt_id) REFERENCES clients.client(clnt_id)
);