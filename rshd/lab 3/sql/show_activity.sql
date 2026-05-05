\pset pager off

SELECT pid,
       usename,
       application_name,
       client_addr,
       backend_type,
       state,
       wait_event_type,
       wait_event,
       xact_start,
       query
FROM pg_stat_activity
WHERE datname = current_database()
ORDER BY application_name, pid;
