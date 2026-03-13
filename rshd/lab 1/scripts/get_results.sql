-- 1) Список всех табличных пространств в кластере
SELECT
    ts.oid                      AS tablespace_oid,
    ts.spcname                  AS tablespace_name,
    pg_get_userbyid(ts.spcowner) AS owner,
    pg_tablespace_location(ts.oid) AS location,
    ts.spcoptions               AS options
FROM pg_tablespace ts
ORDER BY ts.spcname;

-- 2) Список всех объектов в текущей БД с указанием их табличного пространства и размера на диске
WITH db AS (
    SELECT
        datname,
        dattablespace
    FROM pg_database
    WHERE datname = current_database()
),
     defaults AS (
         SELECT
             COALESCE(
                     NULLIF(db.dattablespace, 0),
                     (SELECT oid FROM pg_tablespace WHERE spcname = 'pg_default')
             ) AS db_default_spc
         FROM db
     ),
     objs AS (
         SELECT
             c.oid,
             n.nspname AS schema_name,
             c.relname AS object_name,
             c.relkind,
             COALESCE(NULLIF(c.reltablespace, 0), defaults.db_default_spc) AS effective_spcoid,
             tbl.relname AS index_on_table
         FROM pg_class c
                  JOIN pg_namespace n ON n.oid = c.relnamespace
                  CROSS JOIN defaults
                  LEFT JOIN pg_index ix ON ix.indexrelid = c.oid
                  LEFT JOIN pg_class tbl ON tbl.oid = ix.indrelid
         WHERE n.nspname NOT IN ('pg_catalog','information_schema')
           AND c.relkind IN ('r','i','m','t') -- таблицы, индексы, материализованные представления
     )
SELECT
    t.spcname AS tablespace_name,
    current_database() AS db_name,
    o.schema_name,
    o.object_name,
    CASE o.relkind
        WHEN 'r' THEN 'table'
        WHEN 'i' THEN 'index'
        WHEN 'm' THEN 'matview'
        WHEN 't' THEN 'toast'
        ELSE o.relkind::text
        END AS object_kind,
    o.index_on_table,
    pg_size_pretty(pg_total_relation_size(o.oid)) AS total_size
FROM objs o
         JOIN pg_tablespace t ON t.oid = o.effective_spcoid
ORDER BY
    tablespace_name,
    object_kind,
    total_size DESC,
    schema_name,
    object_name;