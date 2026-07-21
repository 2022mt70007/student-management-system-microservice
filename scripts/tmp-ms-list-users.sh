#!/bin/bash
set -e
cd ~/NewProject
echo "=== postgres containers ==="
sudo docker ps --format '{{.Names}}' | grep -iE 'postgres|mysql|db'
PG=$(sudo docker ps --format '{{.Names}}' | grep -i postgres | head -1)
echo "PG=$PG"
# list dbs and users
sudo docker exec -i "$PG" psql -U sms_user -d postgres -c '\l' 2>/dev/null | head -30 || true
for DB in auth_db sms_auth sms_auth_db; do
  echo "--- $DB ---"
  sudo docker exec -i "$PG" psql -U sms_user -d "$DB" -c "SELECT email, role, failed_login_attempts, account_locked_until FROM users WHERE enabled=true LIMIT 8;" 2>/dev/null && break
done
