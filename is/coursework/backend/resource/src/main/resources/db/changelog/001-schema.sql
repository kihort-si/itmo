-- ===================== ПОЛЬЗОВАТЕЛИ ======================
CREATE TABLE person
(
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    first_name VARCHAR(25) NOT NULL,
    last_name  VARCHAR(25) NOT NULL
);

CREATE TABLE employee
(
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    account_id BIGINT       NOT NULL UNIQUE,
    person_id  BIGINT       NOT NULL UNIQUE,
    role       VARCHAR(255) NOT NULL,
    CONSTRAINT chk_employee_role
        CHECK (role IN (
                        'SALES_MANAGER',
                        'CONSTRUCTOR',
                        'CNC_OPERATOR',
                        'WAREHOUSE_WORKER',
                        'SUPPLY_MANAGER',
                        'ADMIN'
            )),
    CONSTRAINT fk_employee_person
        FOREIGN KEY (person_id) REFERENCES person (id) ON DELETE RESTRICT
);

CREATE TABLE client
(
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email        VARCHAR(50) NOT NULL UNIQUE,
    person_id    BIGINT      NOT NULL UNIQUE,
    account_id   BIGINT      NOT NULL UNIQUE,
    phone_number VARCHAR(20) NOT NULL UNIQUE,
    CONSTRAINT fk_client_person
        FOREIGN KEY (person_id) REFERENCES person (id) ON DELETE RESTRICT
);

-- ===================== ФАЙЛОВАЯ СИСТЕМА ======================

CREATE TABLE file
(
    id                 BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    filename           VARCHAR(255) NOT NULL,
    content_type       VARCHAR(255) NOT NULL,
    current_version_id BIGINT,
    created_at         TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at         TIMESTAMPTZ  NOT NULL DEFAULT now(),
    deleted_at         TIMESTAMPTZ,
    owner_id           BIGINT       NOT NULL,
    CONSTRAINT chk_file_timestamps
        CHECK (
            created_at <= updated_at
                AND (deleted_at IS NULL OR deleted_at >= created_at)
            )
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
    CONSTRAINT fk_file_version_file
        FOREIGN KEY (file_id) REFERENCES file (id) ON DELETE CASCADE
);

ALTER TABLE file
    ADD CONSTRAINT fk_file_current_version
        FOREIGN KEY (current_version_id)
            REFERENCES file_version (id)
            ON DELETE RESTRICT
            DEFERRABLE INITIALLY DEFERRED;

-- ===================== МАТЕРИАЛЬНОЕ ОБЕСПЕЧЕНИЕ ======================

CREATE TABLE material
(
    id                 BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name               VARCHAR(255)   NOT NULL UNIQUE,
    unit_of_measure    VARCHAR(50)    NOT NULL,
    current_balance_id BIGINT,
    order_point        NUMERIC(12, 2) NOT NULL CHECK (order_point >= 0)
);

CREATE TABLE material_balance
(
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    material_id         BIGINT         NOT NULL,
    balance             NUMERIC(12, 2) NOT NULL CHECK (balance >= 0),
    previous_balance_id BIGINT,
    changer_id          BIGINT         NOT NULL,
    changed_at          TIMESTAMPTZ    NOT NULL DEFAULT now(),
    CONSTRAINT fk_mb_material
        FOREIGN KEY (material_id) REFERENCES material (id) ON DELETE CASCADE,
    CONSTRAINT fk_mb_prev
        FOREIGN KEY (previous_balance_id) REFERENCES material_balance (id) ON DELETE SET NULL
);

ALTER TABLE material
    ADD CONSTRAINT fk_material_current_balance
        FOREIGN KEY (current_balance_id)
            REFERENCES material_balance (id)
            ON DELETE RESTRICT
            DEFERRABLE INITIALLY DEFERRED;

CREATE TABLE purchase_order
(
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    current_status_id BIGINT,
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
        FOREIGN KEY (purchase_order_id) REFERENCES purchase_order (id) ON DELETE CASCADE,
    CONSTRAINT chk_purchase_order_status
        CHECK (status IN (
                          'CREATED',
                          'COMPLETED'
            ))
);

ALTER TABLE purchase_order
    ADD CONSTRAINT fk_purchase_order_current_status
        FOREIGN KEY (current_status_id)
            REFERENCES purchase_order_status (id)
            ON DELETE RESTRICT
            DEFERRABLE INITIALLY DEFERRED;

CREATE TABLE purchase_order_material
(
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    material_id       BIGINT         NOT NULL,
    purchase_order_id BIGINT         NOT NULL,
    amount            NUMERIC(12, 2) NOT NULL CHECK (amount > 0),
    price_for_unit    NUMERIC(12, 2) NOT NULL CHECK (price_for_unit >= 0),
    supplier          VARCHAR(255)   NOT NULL,
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

-- ===================== ПРОДУКТ ======================

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

CREATE TABLE required_material
(
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    material_id       BIGINT         NOT NULL,
    product_design_id BIGINT         NOT NULL,
    amount            NUMERIC(12, 2) NOT NULL CHECK (amount > 0),
    CONSTRAINT fk_rqmt_material
        FOREIGN KEY (material_id) REFERENCES material (id) ON DELETE RESTRICT,
    CONSTRAINT fk_rqmt_design
        FOREIGN KEY (product_design_id) REFERENCES product_design (id) ON DELETE CASCADE,
    CONSTRAINT ux_rqmt UNIQUE (material_id, product_design_id)
);

-- ===================== КАТАЛОГ ======================

CREATE TABLE product_catalog
(
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name              VARCHAR(255)   NOT NULL UNIQUE,
    description       TEXT           NOT NULL,
    product_design_id BIGINT         NOT NULL UNIQUE,
    price             NUMERIC(12, 2) NOT NULL CHECK (price >= 0),
    minimal_amount    SMALLINT       NOT NULL CHECK (minimal_amount > 0),
    category          VARCHAR(255)   NOT NULL,
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

-- ===================== ЗАКАЗ ======================

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
    current_status_id     BIGINT,
    client_application_id BIGINT      NOT NULL UNIQUE,
    manager_id            BIGINT      NOT NULL,
    product_design_id     BIGINT      NOT NULL UNIQUE,
    price                 NUMERIC(12, 2) CHECK (price IS NULL OR price >= 0),
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
        FOREIGN KEY (client_order_id) REFERENCES client_order (id) ON DELETE CASCADE,
    CONSTRAINT chk_client_order_status
        CHECK (status IN (
                          'CREATED',
                          'IN_PROGRESS',
                          'PENDING_APPROVAL',
                          'REWORK',
                          'APPROVED',
                          'READY_FOR_PRODUCTION',
                          'IN_PRODUCTION',
                          'READY_FOR_PICKUP',
                          'COMPLETED'
            ))
);

ALTER TABLE client_order
    ADD CONSTRAINT fk_client_order_current_status
        FOREIGN KEY (current_status_id)
            REFERENCES client_order_status (id)
            ON DELETE RESTRICT
            DEFERRABLE INITIALLY DEFERRED;

CREATE TABLE material_consumption
(
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    client_order_id BIGINT         NOT NULL,
    material_id     BIGINT         NOT NULL,
    amount          NUMERIC(12, 2) NOT NULL CHECK (amount >= 0),
    created_at      TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_material_consumption_order
        FOREIGN KEY (client_order_id) REFERENCES client_order (id) ON DELETE RESTRICT,
    CONSTRAINT fk_material_consumption_material
        FOREIGN KEY (material_id) REFERENCES material (id) ON DELETE RESTRICT
);

-- ===================== ЧАТ ======================

CREATE TABLE conversation
(
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    order_id   BIGINT UNIQUE NOT NULL,
    status     VARCHAR(255)  NOT NULL,
    created_at TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_conversation_order
        FOREIGN KEY (order_id) REFERENCES client_order (id) ON DELETE RESTRICT,
    CONSTRAINT chk_conversation_status
        CHECK (status IN (
                          'ACTIVE',
                          'CLOSED'
            ))
);

CREATE TABLE conversation_participant
(
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    conversation_id BIGINT      NOT NULL,
    user_id         BIGINT      NOT NULL,
    joined_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_conversation_participant_conversation_id
        FOREIGN KEY (conversation_id) REFERENCES conversation (id) ON DELETE RESTRICT,
    CONSTRAINT ux_conversation_participant UNIQUE (conversation_id, user_id)
);

CREATE TABLE message
(
    id                          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    content                     TEXT        NOT NULL,
    conversation_participant_id BIGINT      NOT NULL,
    sent_at                     TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_message_conversation_participant_id
        FOREIGN KEY (conversation_participant_id) REFERENCES conversation_participant (id) ON DELETE CASCADE
);

-- ===================== ПРОИЗВОДСТВО ======================

CREATE TABLE production_task
(
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    client_order_id   BIGINT      NOT NULL UNIQUE,
    current_status_id BIGINT,
    started_at        TIMESTAMPTZ,
    finished_at       TIMESTAMPTZ,
    cnc_operator_id   BIGINT      NOT NULL,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_pt_order
        FOREIGN KEY (client_order_id) REFERENCES client_order (id) ON DELETE RESTRICT,
    CONSTRAINT fk_pt_operator
        FOREIGN KEY (cnc_operator_id) REFERENCES employee (id) ON DELETE RESTRICT,
    CONSTRAINT chk_production_task_time
        CHECK (
            finished_at IS NULL
                OR started_at IS NULL
                OR finished_at >= started_at
            )
);

CREATE TABLE production_task_status
(
    id                 BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    production_task_id BIGINT      NOT NULL,
    status             VARCHAR(50) NOT NULL,
    set_at             TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_pts_task
        FOREIGN KEY (production_task_id) REFERENCES production_task (id) ON DELETE CASCADE,
    CONSTRAINT chk_production_task_status
        CHECK (status IN (
                          'QUEUED',
                          'IN_PROGRESS',
                          'COMPLETED'
            ))
);

ALTER TABLE production_task
    ADD CONSTRAINT fk_production_task_current_status
        FOREIGN KEY (current_status_id)
            REFERENCES production_task_status (id)
            ON DELETE RESTRICT
            DEFERRABLE INITIALLY DEFERRED;