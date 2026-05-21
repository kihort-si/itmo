CREATE SCHEMA refs;

CREATE TABLE refs.language(
    lang_id SERIAL PRIMARY KEY,
    code VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL UNIQUE,
    is_default BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE UNIQUE INDEX only_one_default_language
ON refs.language (is_default)
WHERE is_default = TRUE;

CREATE TABLE refs.entity_list_schema(
    eschema_id SERIAL PRIMARY KEY,
    code VARCHAR(255) NOT NULL UNIQUE,
    schema JSONB NOT NULL
);

CREATE TABLE refs.entity_list(
    entl_id SERIAL PRIMARY KEY,
    entity_list_id INTEGER NOT NULL,
    schema_code VARCHAR(255) NOT NULL,
    lang_id INT NOT NULL,
    data JSONB NOT NULL,
    FOREIGN KEY (schema_code) REFERENCES refs.entity_list_schema(code) ON DELETE CASCADE,
    FOREIGN KEY (lang_id) REFERENCES refs.language(lang_id) ON DELETE CASCADE,
    UNIQUE (entity_list_id, schema_code, lang_id)
);

CREATE TABLE refs.entity_single(
    ents_id SERIAL PRIMARY KEY,
    code VARCHAR(255) NOT NULL,
    lang_id INT NOT NULL,
    data JSONB NOT NULL,
    UNIQUE (code, lang_id),
    FOREIGN KEY (lang_id) REFERENCES refs.language(lang_id) ON DELETE CASCADE
);