#!/bin/bash
set -euo pipefail

cd ~/sms-monolith
set -a
source .env
set +a

EMAIL="sidharthsangamam@gmail.com"
MYSQL="sudo docker exec sms-monolith-mysql mysql -u${DB_USER} -p${DB_PASSWORD} ${DB_NAME}"

echo "=== Before ==="
$MYSQL -e "SELECT id, email, status FROM admins;"

echo "=== Removing pending admin + invitation for fresh bootstrap email ==="
$MYSQL -e "DELETE FROM registration_invitations WHERE lower(email)='${EMAIL}';"
$MYSQL -e "DELETE FROM users WHERE lower(email)='${EMAIL}';"
$MYSQL -e "DELETE FROM admins WHERE lower(email)='${EMAIL}';"

ADMIN_COUNT=$($MYSQL -N -e "SELECT COUNT(*) FROM admins;")
echo "Admin count after delete: ${ADMIN_COUNT}"

if [ "${ADMIN_COUNT}" -eq 0 ]; then
  echo "=== Restarting monolith to trigger bootstrap registration email ==="
  sudo docker restart sms-monolith
  sleep 25
  echo "=== After restart ==="
  $MYSQL -e "SELECT id, email, status FROM admins;"
  sudo docker logs sms-monolith --tail 15 2>&1 | grep -iE 'Bootstrap|Registration email|error|failed' || true
else
  echo "Other admins still exist; cannot use bootstrap. Manual JWT create required."
fi
