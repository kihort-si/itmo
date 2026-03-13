#!/bin/sh

# === Переменные окружения ===
export PGDATA=$HOME/ogs68
export PGWAL=$PGDATA/pg_wal
export PGLOCALE=ru_RU.KOI8-R
export PGENCODE=KOI8-R
export PGUSERNAME=postgres0
export PGHOST=localhost
export LANG=ru_RU.KOI8-R
export LC_ALL=ru_RU.KOI8-R

mkdir -p "$PGDATA"

# === Инициализация кластера ===
initdb -D "$PGDATA" \
  --encoding="$PGENCODE" \
  --locale="$PGLOCALE" \
  --lc-collate="$PGLOCALE" \
  --lc-ctype="$PGLOCALE" \
  --lc-messages="$PGLOCALE" \
  --lc-monetary="$PGLOCALE" \
  --lc-numeric="$PGLOCALE" \
  --lc-time="$PGLOCALE" \
  --username="$PGUSERNAME"

pg_ctl -D "$PGDATA" -l "$PGDATA/server.log" start