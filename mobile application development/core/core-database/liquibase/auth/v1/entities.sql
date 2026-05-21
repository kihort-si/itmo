CREATE SCHEMA auth;

CREATE TABLE auth.auth_user (
    user_id SERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    username VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(512) NOT NULL,
    status VARCHAR(64) NOT NULL,
    clnt_id INTEGER UNIQUE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE auth.auth_role (
    role_id SERIAL PRIMARY KEY,
    code VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE auth.auth_user_role (
    user_role_id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL,
    role_id INTEGER NOT NULL,
    FOREIGN KEY (user_id) REFERENCES auth.auth_user(user_id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES auth.auth_role(role_id) ON DELETE CASCADE,
    UNIQUE (user_id, role_id)
);

CREATE TABLE auth.auth_session (
    session_id UUID PRIMARY KEY,
    user_id INTEGER NOT NULL,
    refresh_token_hash VARCHAR(128) NOT NULL,
    status VARCHAR(64) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    last_used_at TIMESTAMP NOT NULL DEFAULT NOW(),
    expires_at TIMESTAMP NOT NULL,
    revoked_at TIMESTAMP,
    ip VARCHAR(255),
    user_agent VARCHAR(1024),
    FOREIGN KEY (user_id) REFERENCES auth.auth_user(user_id) ON DELETE CASCADE
);

CREATE INDEX auth_session_user_id_idx ON auth.auth_session (user_id);
CREATE INDEX auth_session_status_idx ON auth.auth_session (status);
CREATE INDEX auth_auth_user_status_idx ON auth.auth_user (status);

INSERT INTO auth.auth_role (code, name)
VALUES
    ('USER', 'Default application user'),
    ('ADMIN', 'Administrative user');
