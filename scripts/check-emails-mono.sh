#!/bin/bash
cd ~/sms-monolith && source .env
for e in sidharthsidh04931@gmail.com ajai3237wk1@gmail.com; do
  echo "========== $e =========="
  sudo docker exec sms-monolith-mysql mysql -u"${DB_USER}" -p"${DB_PASSWORD}" "${DB_NAME}" -e "
    SELECT 'users' as src, email, role, '' as status FROM users WHERE lower(email)=lower('$e');
    SELECT 'students' as src, email, '' as role, status FROM students WHERE lower(email)=lower('$e');
    SELECT 'teachers' as src, email, '' as role, status FROM teachers WHERE lower(email)=lower('$e');
    SELECT 'admins' as src, email, '' as role, status FROM admins WHERE lower(email)=lower('$e');
    SELECT 'invitation' as src, email, used as role, '' as status FROM registration_invitations WHERE lower(email)=lower('$e');
  " 2>/dev/null
done
