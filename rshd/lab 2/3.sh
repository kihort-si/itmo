psql -h localhost -p 9389 -U postgres0 -d postgres -c "SELECT * FROM lab_test ORDER BY id;"

psql -h localhost -p 9389 -U postgres0 -d postgres -c "SELECT pg_switch_wal();"

rm −rf $PGDATA/pg_wal

pg_ctl −D $PGDATA stop −m immediate
pg_ctl −D $PGDATA −l $PGDATA/server.log start −w

mv $PGDATA ${PGDATA}_damaged
mkdir −p $PGDATA && chmod 700 $PGDATA
rsync −av postgres2@pg133:~/pgbackup/base/ $PGDATA/

mkdir −p ~/ts_idx_restored && chmod 700 ~/ts_idx_restored
rsync −av postgres2@pg133:~/pgbackup/umw28_copy/ ~/ts_idx_restored/
rm −f $PGDATA/pg_tblspc/16394
ln −s /var/db/postgres0/ts_idx_restored $PGDATA/pg_tblspc/16394

ls −l $PGDATA/pg_tblspc/16394

cat >> postgresql.conf <<'EOF'
restore_command = 'scp postgres2@pg133:/var/db/postgres2/pgarchive/%f %p'
recovery_target_timeline = 'latest'
EOF

touch $PGDATA/recovery.signal

pg_ctl −D $PGDATA −l $PGDATA/server.log start −w

psql -h localhost -p 9389 -U postgres0 -d postgres -c "SELECT pg_is_in_recovery();"
psql -h localhost -p 9389 -U postgres0 -d postgres -c "SELECT oid, spcname, pg_tablespace_location(oid) FROM pg_tablespace WHERE spcname = 'ts_idx';"
psql -h localhost -p 9389 -U postgres0 -d postgres -c "SELECT * FROM lab_test ORDER BY id;"