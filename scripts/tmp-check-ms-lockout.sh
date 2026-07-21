#!/bin/bash
set -e
cd ~/NewProject
echo "=== containers ==="
sudo docker ps --format 'table {{.Names}}\t{{.Status}}' | head -30
echo ""
echo "=== auth logs ==="
sudo docker logs sms-auth --tail 80 2>&1 | tail -80
echo ""
echo "=== gateway logs ==="
sudo docker logs sms-gateway --tail 40 2>&1 | tail -40
