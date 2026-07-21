#!/usr/bin/env bash
# Full microservices migration deploy on veritascampus-ms-vm
set -euo pipefail

REPO_DIR="$HOME/NewProject"
BACKUP="$HOME/ms-db-backup.sql"

if [[ ! -d "$REPO_DIR/.git" ]]; then
  rm -rf "$REPO_DIR"
  git clone -b feature/dashboard-update https://github.com/2022mt70007/student-management-system-microservice.git "$REPO_DIR"
fi

cd "$REPO_DIR"
git fetch origin
git checkout feature/dashboard-update
git pull --ff-only origin feature/dashboard-update

cp "$HOME/prod-ms.env" .env

DOCKER="docker"
if ! docker info >/dev/null 2>&1; then
  DOCKER="sudo docker"
fi

echo "==> Starting postgres..."
$DOCKER compose --env-file .env up -d postgres
sleep 25

if [[ -f "$BACKUP" ]]; then
  echo "==> Restoring PostgreSQL backup..."
  $DOCKER exec -i sms-postgres psql -U sms_user -d postgres < "$BACKUP" || true
fi

echo "==> Starting all services..."
$DOCKER compose --env-file .env up -d --build

sleep 20

echo "==> Building frontend..."
cd frontend
npm ci
npm run build

sudo mkdir -p /var/www/veritascampus-me
sudo rsync -a --delete dist/ /var/www/veritascampus-me/

sudo cp "$HOME/nginx-veritascampus-me.conf" /etc/nginx/sites-available/veritascampus-me
sudo ln -sf /etc/nginx/sites-available/veritascampus-me /etc/nginx/sites-enabled/veritascampus-me
sudo rm -f /etc/nginx/sites-enabled/default
sudo nginx -t
sudo systemctl reload nginx

echo "==> Microservices deploy complete"
