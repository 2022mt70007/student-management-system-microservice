#!/usr/bin/env bash
# Full monolith migration deploy on veritascampus-mono-vm
set -euo pipefail

REPO_DIR="$HOME/sms-monolith"
BACKUP="$HOME/mono-db-backup.sql"

if [[ ! -d "$REPO_DIR/.git" ]]; then
  git clone -b feature/firstBrach https://github.com/2022mt70007/student-management-system-monolithic.git "$REPO_DIR"
fi

cd "$REPO_DIR"
git fetch origin
git checkout feature/firstBrach
git pull --ff-only origin feature/firstBrach || true

cp "$HOME/prod-mono.env" .env

DOCKER="docker"
if ! docker info >/dev/null 2>&1; then
  DOCKER="sudo docker"
fi

echo "==> Starting backend..."
$DOCKER compose -f docker-compose.prod.yml --env-file .env up -d --build

sleep 25

if [[ -f "$BACKUP" ]]; then
  echo "==> Restoring MySQL backup..."
  $DOCKER exec -i sms-monolith-mysql mysql -u sms_user -p"${DB_PASSWORD:-$(grep ^DB_PASSWORD= .env | cut -d= -f2)}" sms_monolith_db < "$BACKUP" || true
fi

echo "==> Building frontend..."
cd frontend
npm ci
npm run build

sudo mkdir -p /var/www/veritascampus
sudo rsync -a --delete dist/ /var/www/veritascampus/

sudo cp "$REPO_DIR/docs/nginx-veritascampus.conf" /etc/nginx/sites-available/veritascampus
sudo ln -sf /etc/nginx/sites-available/veritascampus /etc/nginx/sites-enabled/veritascampus
sudo rm -f /etc/nginx/sites-enabled/default
sudo nginx -t
sudo systemctl reload nginx

echo "==> Monolith deploy complete"
