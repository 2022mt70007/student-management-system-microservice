#!/bin/bash
set -euo pipefail

cd ~/sms-monolith
set -a
source .env
set +a

EMAIL="sidharthsangamam@gmail.com"
BODY='{"name":"Sidharth Sangam","adminId":"ADMIN-MONO","department":"Administration","email":"sidharthsangamam@gmail.com","phone":"","address":""}'

ADMIN_ID=$(sudo docker exec sms-monolith-mysql mysql -u"${DB_USER}" -p"${DB_PASSWORD}" "${DB_NAME}" -N \
  -e "SELECT id FROM admins WHERE lower(email)='${EMAIL}' LIMIT 1;" 2>/dev/null || true)

if [ -n "${ADMIN_ID}" ]; then
  echo "Deleting existing admin id=${ADMIN_ID}"
  curl -sS -X DELETE "http://localhost:8090/api/admin/admins/${ADMIN_ID}"
  echo
fi

echo "Creating admin and sending fresh registration email"
curl -sS -w "\nHTTP %{http_code}\n" -X POST http://localhost:8090/api/admin/admins \
  -H "Content-Type: application/json" \
  -d "${BODY}"

echo "=== Latest monolith email logs ==="
sudo docker logs sms-monolith --tail 10 2>&1 | grep -iE 'Registration email|error|mail' || true
