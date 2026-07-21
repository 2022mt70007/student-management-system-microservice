#!/bin/bash
set -euo pipefail

cd ~/sms-monolith
set -a
source .env
set +a

EMAIL="sidharthsangamam@gmail.com"
BODY='{"name":"Sidharth Sangam","adminId":"ADMIN-MONO","department":"Administration","email":"sidharthsangamam@gmail.com","phone":"","address":""}'

echo "=== Existing admins ==="
sudo docker exec sms-monolith-mysql mysql -u"${DB_USER}" -p"${DB_PASSWORD}" "${DB_NAME}" \
  -e "SELECT id, email, status FROM admins;"

EXISTING=$(sudo docker exec sms-monolith-mysql mysql -u"${DB_USER}" -p"${DB_PASSWORD}" "${DB_NAME}" -N \
  -e "SELECT COUNT(*) FROM admins WHERE lower(email)='${EMAIL}';")

if [ "${EXISTING}" -gt 0 ]; then
  echo "Admin already exists for ${EMAIL}; skipping create."
else
  echo "=== Creating admin and sending registration email ==="
  curl -sS -w "\nHTTP %{http_code}\n" -X POST http://localhost:8090/api/admin/admins \
    -H "Content-Type: application/json" \
    -d "${BODY}"
fi

echo "=== Auth invitations for ${EMAIL} ==="
sudo docker exec sms-monolith-mysql mysql -u"${DB_USER}" -p"${DB_PASSWORD}" "${DB_NAME}" \
  -e "SELECT email, used, expires_at FROM registration_invitations WHERE lower(email)='${EMAIL}';"

echo "=== Monolith logs (email) ==="
sudo docker logs sms-monolith --tail 30 2>&1 | grep -iE 'mail|email|registration|ses|error' || true
