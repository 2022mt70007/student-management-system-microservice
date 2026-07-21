#!/bin/bash
set -e
cd ~/sms-monolith
set -a
# shellcheck disable=SC1091
source .env
set +a
USER="${DB_USER:-sms_user}"
PASS="${DB_PASSWORD:-sms_pass}"
DB="${DB_NAME:-sms_monolith_db}"
sudo docker exec -i sms-monolith-mysql mysql -u"$USER" -p"$PASS" "$DB" -e \
  "UPDATE users SET failed_login_attempts=0, account_locked_until=NULL WHERE email='sidharthsidh04931@gmail.com'; SELECT email, failed_login_attempts, account_locked_until FROM users WHERE email='sidharthsidh04931@gmail.com';"
