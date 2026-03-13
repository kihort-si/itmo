# === Создание директории ===
mkdir -p "$HOME/umw28"
chmod 700 "$HOME/umw28"

# === Вход в psql ===
with admin permissions
psql -p 9389 -d postgres
