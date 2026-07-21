#!/bin/bash
set -euo pipefail

EMAIL="sidharthsangamam@gmail.com"
BODY='{"name":"Sidharth Sangam","adminId":"ADMIN-SMS","department":"Administration","email":"sidharthsangamam@gmail.com","phone":"","address":""}'

echo "=== Existing admins ==="
sudo docker exec sms-postgres psql -U sms_user -d admin_db -c "SELECT id, email, status FROM admins;"

if sudo docker exec sms-postgres psql -U sms_user -d admin_db -tAc "SELECT 1 FROM admins WHERE lower(email)='${EMAIL}'" | grep -q 1; then
  echo "Admin already exists for ${EMAIL}; skipping create."
else
  echo "=== Creating admin and sending registration email ==="
  curl -sS -w "\nHTTP %{http_code}\n" -X POST http://localhost:8082/api/admin/admins \
    -H "Content-Type: application/json" \
    -d "${BODY}"
fi

echo "=== Auth invitations for ${EMAIL} ==="
sudo docker exec sms-postgres psql -U sms_user -d auth_db -c \
  "SELECT email, used, expires_at FROM registration_invitations WHERE lower(email)='${EMAIL}';"

echo "=== Admin-service logs (email) ==="
sudo docker logs admin-service --tail 30 2>&1 | grep -iE 'mail|email|registration|ses|error' || true
