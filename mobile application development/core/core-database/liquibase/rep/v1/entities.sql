CREATE SCHEMA rep;

CREATE TABLE rep.template (
    tmpl_id     UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    code        VARCHAR(128) NOT NULL UNIQUE,
    name        VARCHAR(512) NOT NULL,
    channel     VARCHAR(16)  NOT NULL,          -- EMAIL | PUSH
    subject     VARCHAR(512),                   -- only for EMAIL channel
    body        TEXT         NOT NULL,           -- Mustache template
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX rep_template_code_idx ON rep.template (code);
