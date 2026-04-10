rsync −av postgres0@pg127:/tmp/basebackup_s2/ ~/pgdata_pitr/
rsync −av postgres0@pg127:/tmp/ts_idx_s2/ /tmp/ts_idx_pitr/

rm −f ~/pgdata_pitr/pg_tblspc/16394
ln −s /tmp/ts_idx_pitr ~/pgdata_pitr/pg_tblspc/16394

cat >> postgresql.auto.conf <<'EOF'
restore_command = 'cp /var/db/postgres2/pgarchive/%f %p'
recovery_target_time = '2026−03−28 17:24:49.629874+03'
recovery_target_action = 'promote'
recovery_target_timeline = 'latest'
EOF

cat >> postgresql.conf <<'EOF'
archive_command = '/bin/true'
unix_socket_directories = '/tmp'
EOF

touch ~/pgdata_pitr/recovery.signal
pg_ctl −D ~/pgdata_pitr −l ~/pgdata_pitr/server.log start −w

pg_dump −h localhost −p 9389 −U postgres0 −d darkorangemon −t lab_test −−data−only −−column−inserts −f ~/lab_test_dump.sql

scp postgres2@pg133:~/lab_test_dump.sql /tmp/lab_test_dump.sql
sed 's/public\.lab_test/public.lab_test_restore/g' /tmp/lab_test_dump.sql > /tmp/lab_test_dump_restore.sql

psql −h localhost −p 9389 −U postgres0 −d darkorangemon −f /tmp/lab_test_dump_restore.sql
