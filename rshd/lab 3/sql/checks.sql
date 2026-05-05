\pset pager off

SELECT current_database() AS db_name,
       current_user AS db_user,
       inet_server_addr() AS server_addr,
       inet_server_port() AS server_port,
       pg_is_in_recovery() AS in_recovery,
       CASE
         WHEN pg_is_in_recovery() THEN pg_last_wal_replay_lsn()
         ELSE pg_current_wal_lsn()
       END AS visible_lsn;

SELECT * FROM accounts ORDER BY id;
SELECT * FROM transfers ORDER BY id;
