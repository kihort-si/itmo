CREATE TABLE account
(
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    username   VARCHAR(50)  NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    enabled    BOOLEAN      NOT NULL DEFAULT FALSE,
    role       VARCHAR(255) NOT NULL,
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT chk_account_role
        CHECK (role IN (
                        'CLIENT',
                        'SALES_MANAGER',
                        'CONSTRUCTOR',
                        'CNC_OPERATOR',
                        'WAREHOUSE_WORKER',
                        'SUPPLY_MANAGER',
                        'ADMIN'
            )),
    CONSTRAINT chk_account_timestamps
        CHECK (
            created_at <= updated_at
            )
);

CREATE TABLE email_token
(
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    token         VARCHAR(255) NOT NULL UNIQUE,
    account_id     BIGINT       NOT NULL,
    expiration_dt TIMESTAMPTZ  NOT NULL,
    creation_dt   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT fk_email_token_client
        FOREIGN KEY (account_id) REFERENCES account (id) ON DELETE CASCADE
);