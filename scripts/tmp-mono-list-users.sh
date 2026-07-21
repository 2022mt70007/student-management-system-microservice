#!/bin/bash
set -e
cd ~/sms-monolith
# load DB password from .env without printing secrets
set -a
# shellcheck disable=SC1091
source .env
set +a
USER="${DB_USER:-sms_user}"
PASS="${DB_PASSWORD:-sms_pass}"
DB="${DB_NAME:-sms_monolith_db}"
sudo docker exec -i sms-monolith-mysql mysql -u"$USER" -p"$PASS" "$DB" -e \
  "SELECT email, role, failed_login_attempts, account_locked_until, enabled FROM users LIMIT 10;"
