#!/bin/bash
set -e
for i in 1 2 3 4 5 6 7 8 9 10; do
  if sudo docker logs sms-auth 2>&1 | grep -q 'Started AuthServiceApplication'; then
    echo AUTH_READY
    break
  fi
  echo "waiting auth... $i"
  sleep 10
done
sudo docker logs sms-auth --tail 20 2>&1 | tail -20
sudo docker logs sms-gateway --tail 10 2>&1 | tail -10
