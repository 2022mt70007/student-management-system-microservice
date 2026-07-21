#!/bin/bash
EMAILS="sidharthsidh04931@gmail.com ajai3237wk1@gmail.com"
for e in $EMAILS; do
  echo "========== $e =========="
  echo "--- auth users ---"
  sudo docker exec sms-postgres psql -U sms_user -d auth_db -tAc "SELECT COALESCE(role::text,'') || ' | enabled=' || enabled FROM users WHERE lower(email)=lower('$e');"
  echo "--- invitations ---"
  sudo docker exec sms-postgres psql -U sms_user -d auth_db -c "SELECT email, code, used, expires_at FROM registration_invitations WHERE lower(email)=lower('$e');"
  echo "--- students ---"
  sudo docker exec sms-postgres psql -U sms_user -d student_db -c "SELECT id, email, status FROM students WHERE lower(email)=lower('$e');"
  echo "--- teachers ---"
  sudo docker exec sms-postgres psql -U sms_user -d teacher_db -c "SELECT id, email, status FROM teachers WHERE lower(email)=lower('$e');"
  echo "--- admins ---"
  sudo docker exec sms-postgres psql -U sms_user -d admin_db -c "SELECT id, email, status FROM admins WHERE lower(email)=lower('$e');"
done
