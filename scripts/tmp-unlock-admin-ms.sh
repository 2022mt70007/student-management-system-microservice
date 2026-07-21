#!/bin/bash
set -e
sudo docker exec -i sms-postgres psql -U sms_user -d auth_db <<'SQL'
UPDATE users
SET failed_login_attempts = 0,
    account_locked_until = NULL
WHERE email = 'sidharthsangamam@gmail.com';
SELECT email, failed_login_attempts, account_locked_until
FROM users
WHERE email = 'sidharthsangamam@gmail.com';
SQL
