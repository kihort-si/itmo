CREATE TABLE account
(
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    username   VARCHAR(50)  NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    enabled    BOOLEAN      NOT NULL DEFAULT FALSE,
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE message
(
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    content     TEXT        NOT NULL,
    sender_id   BIGINT      NOT NULL,
    receiver_id BIGINT      NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_message_sender
        FOREIGN KEY (sender_id) REFERENCES account (id) ON DELETE CASCADE,
    CONSTRAINT fk_message_receiver
        FOREIGN KEY (receiver_id) REFERENCES account (id) ON DELETE CASCADE
);

CREATE TABLE person
(
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    first_name VARCHAR(25) NOT NULL,
    last_name  VARCHAR(25) NOT NULL
);

CREATE TABLE employee
(
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    account_id BIGINT NOT NULL UNIQUE,
    person_id  BIGINT NOT NULL UNIQUE,
    CONSTRAINT fk_employee_account
        FOREIGN KEY (account_id) REFERENCES account (id) ON DELETE RESTRICT,
    CONSTRAINT fk_employee_person
        FOREIGN KEY (person_id) REFERENCES person (id) ON DELETE RESTRICT
);

CREATE TABLE client
(
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email        VARCHAR(50)  NOT NULL UNIQUE,
    person_id    BIGINT       NOT NULL UNIQUE,
    account_id   BIGINT       NOT NULL UNIQUE,
    phone_number VARCHAR(20)  NOT NULL UNIQUE,
    address      VARCHAR(255) NOT NULL UNIQUE,
    CONSTRAINT fk_client_person
        FOREIGN KEY (person_id) REFERENCES person (id) ON DELETE RESTRICT,
    CONSTRAINT fk_client_account
        FOREIGN KEY (account_id) REFERENCES account (id) ON DELETE RESTRICT
);

CREATE TABLE email_token
(
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    token         VARCHAR(255) NOT NULL UNIQUE,
    client_id     BIGINT       NOT NULL,
    expiration_dt TIMESTAMPTZ  NOT NULL,
    creation_dt   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT fk_email_token_client
        FOREIGN KEY (client_id) REFERENCES client (id) ON DELETE CASCADE
);

CREATE TABLE file
(
    id                 BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    filename           VARCHAR(255)  NOT NULL,
    content_type       VARCHAR(255)  NOT NULL,
    current_version_id BIGINT UNIQUE NOT NULL,
    created_at         TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at         TIMESTAMPTZ   NOT NULL DEFAULT now(),
    deleted_at         TIMESTAMPTZ,
    owner_id           BIGINT        NOT NULL,
    CONSTRAINT fk_file_owner
        FOREIGN KEY (owner_id) REFERENCES account (id) ON DELETE RESTRICT
);

CREATE TABLE file_version
(
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    creator_id   BIGINT       NOT NULL,
    bucket       VARCHAR(63)  NOT NULL,
    object_key   TEXT         NOT NULL,
    size_bytes   BIGINT       NOT NULL CHECK (size_bytes >= 0),
    content_type VARCHAR(255) NOT NULL,
    uploaded_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    file_id      BIGINT       NOT NULL,
    CONSTRAINT fk_file_version_creator
        FOREIGN KEY (creator_id) REFERENCES account (id) ON DELETE RESTRICT,
    CONSTRAINT fk_file_version_file
        FOREIGN KEY (file_id) REFERENCES file (id) ON DELETE CASCADE
);

ALTER TABLE file
    ADD CONSTRAINT fk_file_current_version
        FOREIGN KEY (current_version_id)
            REFERENCES file_version (id)
            ON DELETE RESTRICT
            DEFERRABLE INITIALLY DEFERRED;

CREATE TABLE product_design
(
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    constructor_id BIGINT, -- NULL если ещё не назначен
    product_name   VARCHAR(50) NOT NULL,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_product_design_constructor
        FOREIGN KEY (constructor_id) REFERENCES employee (id) ON DELETE SET NULL
);

CREATE TABLE product_catalog
(
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name              VARCHAR(255) NOT NULL UNIQUE,
    description       TEXT         NOT NULL,
    product_design_id BIGINT       NOT NULL UNIQUE,
    price             REAL         NOT NULL CHECK (price >= 0),
    minimal_amount    SMALLINT     NOT NULL CHECK (minimal_amount > 0),
    CONSTRAINT fk_product_catalog_design
        FOREIGN KEY (product_design_id) REFERENCES product_design (id) ON DELETE RESTRICT
);

CREATE TABLE product_photo
(
    id                 BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    file_id            BIGINT NOT NULL,
    product_catalog_id BIGINT NOT NULL,
    CONSTRAINT fk_product_photo_file
        FOREIGN KEY (file_id) REFERENCES file (id) ON DELETE CASCADE,
    CONSTRAINT fk_product_photo_catalog
        FOREIGN KEY (product_catalog_id) REFERENCES product_catalog (id) ON DELETE CASCADE
);

CREATE TABLE product_design_file
(
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    product_design_id BIGINT      NOT NULL,
    file_id           BIGINT      NOT NULL,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_pdf_design
        FOREIGN KEY (product_design_id) REFERENCES product_design (id) ON DELETE CASCADE,
    CONSTRAINT fk_pdf_file
        FOREIGN KEY (file_id) REFERENCES file (id) ON DELETE CASCADE,
    CONSTRAINT ux_pdf_design_file UNIQUE (product_design_id, file_id)
);

CREATE TABLE material
(
    id                 BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name               VARCHAR(255)  NOT NULL UNIQUE,
    unit_of_measure    VARCHAR(50)   NOT NULL,
    current_balance_id BIGINT UNIQUE NOT NULL,
    order_point        REAL          NOT NULL CHECK (order_point >= 0)
);

CREATE TABLE material_balance
(
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    material_id         BIGINT      NOT NULL,
    balance             REAL        NOT NULL,
    previous_balance_id BIGINT,
    changer_id          BIGINT      NOT NULL,
    changed_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_mb_material
        FOREIGN KEY (material_id) REFERENCES material (id) ON DELETE CASCADE,
    CONSTRAINT fk_mb_prev
        FOREIGN KEY (previous_balance_id) REFERENCES material_balance (id) ON DELETE SET NULL,
    CONSTRAINT fk_mb_changer
        FOREIGN KEY (changer_id) REFERENCES account (id) ON DELETE RESTRICT
);

ALTER TABLE material
    ADD CONSTRAINT fk_material_current_balance
        FOREIGN KEY (current_balance_id)
            REFERENCES material_balance (id)
            ON DELETE RESTRICT
            DEFERRABLE INITIALLY DEFERRED;

CREATE TABLE required_material
(
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    material_id       BIGINT NOT NULL,
    product_design_id BIGINT NOT NULL,
    amount            REAL   NOT NULL CHECK (amount > 0),
    CONSTRAINT fk_rqmt_material
        FOREIGN KEY (material_id) REFERENCES material (id) ON DELETE RESTRICT,
    CONSTRAINT fk_rqmt_design
        FOREIGN KEY (product_design_id) REFERENCES product_design (id) ON DELETE CASCADE,
    CONSTRAINT ux_rqmt UNIQUE (material_id, product_design_id)
);

CREATE TABLE client_application
(
    id                         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    client_id                  BIGINT      NOT NULL,
    description                TEXT        NOT NULL,
    created_at                 TIMESTAMPTZ NOT NULL DEFAULT now(),
    template_product_design_id BIGINT,
    amount                     SMALLINT    NOT NULL CHECK (amount > 0),
    CONSTRAINT fk_clapp_client
        FOREIGN KEY (client_id) REFERENCES client (id) ON DELETE CASCADE,
    CONSTRAINT fk_clapp_template_design
        FOREIGN KEY (template_product_design_id) REFERENCES product_design (id) ON DELETE SET NULL
);

CREATE TABLE client_application_attachment
(
    id                    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    client_application_id BIGINT NOT NULL,
    file_id               BIGINT NOT NULL UNIQUE,
    CONSTRAINT fk_clappatt_clapp
        FOREIGN KEY (client_application_id) REFERENCES client_application (id) ON DELETE CASCADE,
    CONSTRAINT fk_clappatt_file
        FOREIGN KEY (file_id) REFERENCES file (id) ON DELETE RESTRICT
);

CREATE TABLE client_order
(
    id                    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    current_status_id     BIGINT      NOT NULL UNIQUE,
    client_application_id BIGINT      NOT NULL UNIQUE,
    manager_id            BIGINT      NOT NULL,
    product_design_id     BIGINT      NOT NULL UNIQUE,
    price                 REAL CHECK (price IS NULL OR price >= 0),
    created_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_client_order_client_application
        FOREIGN KEY (client_application_id) REFERENCES client_application (id) ON DELETE RESTRICT,
    CONSTRAINT fk_client_order_manager
        FOREIGN KEY (manager_id) REFERENCES employee (id) ON DELETE RESTRICT,
    CONSTRAINT fk_client_order_product_design
        FOREIGN KEY (product_design_id) REFERENCES product_design (id) ON DELETE RESTRICT
);

CREATE TABLE client_order_status
(
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    client_order_id BIGINT      NOT NULL,
    status          VARCHAR(50) NOT NULL,
    set_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_client_order_status_order
        FOREIGN KEY (client_order_id) REFERENCES client_order (id) ON DELETE CASCADE
);

ALTER TABLE client_order
    ADD CONSTRAINT fk_client_order_current_status
        FOREIGN KEY (current_status_id)
            REFERENCES client_order_status (id)
            ON DELETE RESTRICT
            DEFERRABLE INITIALLY DEFERRED;

CREATE TABLE material_consumption
(
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    order_id    BIGINT      NOT NULL,
    material_id BIGINT      NOT NULL,
    amount      REAL        NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_material_consumption_order
        FOREIGN KEY (order_id) REFERENCES client_order (id) ON DELETE RESTRICT,
    CONSTRAINT fk_material_consumption_material
        FOREIGN KEY (material_id) REFERENCES material (id) ON DELETE RESTRICT,
    CONSTRAINT ux_cons
        UNIQUE (order_id, material_id, created_at)
);

CREATE TABLE production_task
(
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    product_design_id BIGINT      NOT NULL UNIQUE,
    current_status_id BIGINT      NOT NULL UNIQUE,
    started_at        TIMESTAMPTZ NOT NULL,
    finished_at       TIMESTAMPTZ NOT NULL,
    cnc_operator_id   BIGINT      NOT NULL,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_pt_time CHECK (finished_at >= started_at),
    CONSTRAINT fk_pt_design
        FOREIGN KEY (product_design_id) REFERENCES product_design (id) ON DELETE RESTRICT,
    CONSTRAINT fk_pt_operator
        FOREIGN KEY (cnc_operator_id) REFERENCES employee (id) ON DELETE RESTRICT
);

CREATE TABLE production_task_status
(
    id                 BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    production_task_id BIGINT      NOT NULL,
    status             VARCHAR(50) NOT NULL,
    set_at             TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_pts_task
        FOREIGN KEY (production_task_id) REFERENCES production_task (id) ON DELETE CASCADE
);

ALTER TABLE production_task
    ADD CONSTRAINT fk_pt_current_status
        FOREIGN KEY (current_status_id)
            REFERENCES production_task_status (id)
            ON DELETE RESTRICT
            DEFERRABLE INITIALLY DEFERRED;

CREATE TABLE production_issue
(
    id                 BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    production_task_id BIGINT      NOT NULL,
    occurred_at        timestamptz NOT NULL DEFAULT now(),
    severity           VARCHAR(20) NOT NULL,
    description        TEXT        NOT NULL,
    CONSTRAINT fk_production_issue_task
        FOREIGN KEY (production_task_id) REFERENCES production_task (id) ON DELETE CASCADE
);

CREATE TABLE purchase_order
(
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    current_status_id BIGINT      NOT NULL UNIQUE,
    supply_manager_id BIGINT      NOT NULL,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_po_manager
        FOREIGN KEY (supply_manager_id) REFERENCES employee (id) ON DELETE RESTRICT
);

CREATE TABLE purchase_order_status
(
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    purchase_order_id BIGINT      NOT NULL,
    status            VARCHAR(50) NOT NULL,
    set_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_pos_po
        FOREIGN KEY (purchase_order_id) REFERENCES purchase_order (id) ON DELETE CASCADE
);

ALTER TABLE purchase_order
    ADD CONSTRAINT fk_po_current_status
        FOREIGN KEY (current_status_id)
            REFERENCES purchase_order_status (id)
            ON DELETE RESTRICT
            DEFERRABLE INITIALLY DEFERRED;

CREATE TABLE purchase_order_material
(
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    material_id       BIGINT       NOT NULL,
    purchase_order_id BIGINT       NOT NULL,
    amount            REAL         NOT NULL CHECK (amount > 0),
    price_for_unit    REAL         NOT NULL CHECK (price_for_unit >= 0),
    supplier          VARCHAR(255) NOT NULL,
    CONSTRAINT fk_pom_material
        FOREIGN KEY (material_id) REFERENCES material (id) ON DELETE RESTRICT,
    CONSTRAINT fk_pom_po
        FOREIGN KEY (purchase_order_id) REFERENCES purchase_order (id) ON DELETE CASCADE,
    CONSTRAINT ux_pom UNIQUE (material_id, purchase_order_id)
);

CREATE TABLE purchase_order_receipt
(
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    purchase_order_id   BIGINT      NOT NULL UNIQUE,
    warehouse_worker_id BIGINT      NOT NULL,
    invoice_number      VARCHAR(20) NOT NULL,
    receipted_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_por_po
        FOREIGN KEY (purchase_order_id) REFERENCES purchase_order (id) ON DELETE RESTRICT,
    CONSTRAINT fk_por_worker
        FOREIGN KEY (warehouse_worker_id) REFERENCES employee (id) ON DELETE RESTRICT
);
