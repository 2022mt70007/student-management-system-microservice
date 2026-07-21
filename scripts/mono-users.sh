#!/bin/bash
cd ~/sms-monolith && source .env
sudo docker exec sms-monolith-mysql mysql -u"${DB_USER}" -p"${DB_PASSWORD}" "${DB_NAME}" -e "
SELECT 'users' as t, email, role FROM users;
SELECT 'students' as t, email, status FROM students;
SELECT 'teachers' as t, email, status FROM teachers;
" 2>/dev/null
