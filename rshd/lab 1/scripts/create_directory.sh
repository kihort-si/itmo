CREATE TABLESPACE ts_idx LOCATION '/var/db/postgres0/umw28';
-- Создание
table space
create database darkorangemon template template1;

-- Создание ролей и настройка пароля
SET password_encryption = 'md5';
CREATE ROLE darkrole LOGIN PASSWORD 'admin' NOSUPERUSER NOCREATEDB NOCREATEROLE NOINHERIT;
-- Добавление разрешений
GRANT CREATE ON TABLESPACE ts_idx TO darkrole;
GRANT CONNECT, CREATE ON DATABASE darkorangemon TO darkrole;

-- Подключение к бд darkorangemon
\c darkorangemon
-- Добавление разрешений к схеме
GRANT USAGE, CREATE ON SCHEMA public TO darkrole;
