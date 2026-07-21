#!/bin/bash
cd ~/sms-monolith && source .env
sudo docker exec sms-monolith-mysql mysql -u"${DB_USER}" -p"${DB_PASSWORD}" "${DB_NAME}" \
  -e "SELECT email, code, expires_at FROM registration_invitations WHERE email='sidharthsangamam@gmail.com';"
