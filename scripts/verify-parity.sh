#!/bin/bash
set -euo pipefail

MODE="${1:-ms}"

if [ "$MODE" = "ms" ]; then
  echo "=== MICROSERVICES (veritascampus.me) ==="
  grep -E 'REGISTRATION_BASE_URL|JWT_SECRET|BOOTSTRAP|MAIL_FROM' ~/NewProject/.env 2>/dev/null | sed 's/PASSWORD=.*/***/; s/SECRET=.*/SECRET=***/'
  echo "--- containers ---"
  sudo docker compose -f ~/NewProject/docker-compose.yml ps --format '{{.Name}}: {{.Status}}' | wc -l
  echo "--- academic ---"
  sudo docker exec sms-postgres psql -U sms_user -d course_db -tAc "SELECT 'depts='||count(*) FROM departments; SELECT 'classes='||count(*) FROM academic_classes; SELECT 'subjects='||count(*) FROM subjects;"
  echo "--- users ---"
  sudo docker exec sms-postgres psql -U sms_user -d auth_db -c "SELECT email, role, enabled FROM users ORDER BY email;"
  sudo docker exec sms-postgres psql -U sms_user -d admin_db -c "SELECT email, status FROM admins ORDER BY email;"
  sudo docker exec sms-postgres psql -U sms_user -d student_db -c "SELECT count(*) as students FROM students;"
  sudo docker exec sms-postgres psql -U sms_user -d teacher_db -c "SELECT count(*) as teachers FROM teachers;"
else
  echo "=== MONOLITH (veritascampus.page) ==="
  grep -E 'REGISTRATION_BASE_URL|JWT_SECRET|BOOTSTRAP|MAIL_FROM' ~/sms-monolith/.env 2>/dev/null | sed 's/PASSWORD=.*/***/; s/SECRET=.*/SECRET=***/'
  cd ~/sms-monolith && source .env
  echo "--- containers ---"
  sudo docker compose -f docker-compose.prod.yml ps --format '{{.Name}}: {{.Status}}'
  echo "--- academic ---"
  sudo docker exec sms-monolith-mysql mysql -u"${DB_USER}" -p"${DB_PASSWORD}" "${DB_NAME}" -N -e "SELECT CONCAT('depts=',COUNT(*)) FROM departments; SELECT CONCAT('classes=',COUNT(*)) FROM academic_classes; SELECT CONCAT('subjects=',COUNT(*)) FROM subjects;" 2>/dev/null
  echo "--- users ---"
  sudo docker exec sms-monolith-mysql mysql -u"${DB_USER}" -p"${DB_PASSWORD}" "${DB_NAME}" -e "SELECT email, role, enabled FROM users ORDER BY email;" 2>/dev/null
  sudo docker exec sms-monolith-mysql mysql -u"${DB_USER}" -p"${DB_PASSWORD}" "${DB_NAME}" -e "SELECT email, status FROM admins ORDER BY email;" 2>/dev/null
  sudo docker exec sms-monolith-mysql mysql -u"${DB_USER}" -p"${DB_PASSWORD}" "${DB_NAME}" -e "SELECT COUNT(*) as students FROM students; SELECT COUNT(*) as teachers FROM teachers;" 2>/dev/null
fi

echo "--- health ---"
curl -s -o /dev/null -w "localhost HTTP: %{http_code}\n" http://localhost:80/ 2>/dev/null || true
