cat >> postgresql.conf <<'EOF'
wal_level = replica
archive_mode = on
archive_command = 'scp %p postgres2@pg133:/var/db/postgres2/pgarchive/%f'
archive_timeout = 60s
max_wal_senders = 5
EOF

psql -h localhost -p 9389 -U postgres0 -d postgres -c "CREATE ROLE backup_user WITH LOGIN REPLICATION PASSWORD '12345';"
ssh−keygen −t ed25519
ssh−copy−id postgres2@pg133
ssh postgres2@pg133 'mkdir −p ~/pgarchive ~/pgdata && ls −ld ~/pgarchive ~/pgdata'
systemctl restart postgresql

psql -h localhost -p 9389 -U postgres0 -d postgres -c "SHOW archive_mode;"
psql -h localhost -p 9389 -U postgres0 -d postgres -c "SHOW archive_command;"
psql -h localhost -p 9389 -U postgres0 -d postgres -c "SHOW wal_level;"

mkdir −p /tmp/umw28_copy
rm −rf /tmp/umw28_copy/*

pg_basebackup −h 127.0.0.1 −p 9389 −U backup_user −D /tmp/basebackup −Fp −Xs −P −v −T /var/db/postgres0/umw28=/tmp/umw28_copy

rsync −av /tmp/basebackup/ postgres2@pg133:~/pgdata/

ls ~/pgdata

cat >> postgresql.conf <<'EOF'
restore_command = 'cp /var/db/postgres2/pgarchive/%f %p'
EOF

