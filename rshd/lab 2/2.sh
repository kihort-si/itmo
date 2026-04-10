cat >> postgresql.auto.conf <<'EOF'
restore_command = 'cp /var/db/postgres2/pgarchive/%f %p'
recovery_target_timeline = 'latest'
EOF

cat >> postgresql.conf <<'EOF'
archive_command = '/bin/true'
unix_socket_directories = '/tmp'
EOF

touch ~/pgdata/recovery.signal

pg_ctl −D ~/pgdata −l ~/pgdata/server.log start −w
