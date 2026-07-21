#!/bin/bash
sudo docker exec sms-postgres psql -U sms_user -d auth_db -c "SELECT email, code, expires_at FROM registration_invitations WHERE email='sidharthsangamam@gmail.com';"
