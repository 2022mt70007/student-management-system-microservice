-- Run once on existing auth_db volumes created before login lockout columns were added.
-- Example: docker exec -i sms-postgres psql -U sms_user -d auth_db < docker/migrations/001-auth-user-lockout.sql

ALTER TABLE users ADD COLUMN IF NOT EXISTS account_locked_until timestamp(6);
ALTER TABLE users ADD COLUMN IF NOT EXISTS failed_login_attempts integer;
UPDATE users SET failed_login_attempts = 0 WHERE failed_login_attempts IS NULL;
ALTER TABLE users ALTER COLUMN failed_login_attempts SET DEFAULT 0;
ALTER TABLE users ALTER COLUMN failed_login_attempts SET NOT NULL;
