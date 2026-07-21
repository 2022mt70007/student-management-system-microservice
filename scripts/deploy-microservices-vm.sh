#!/usr/bin/env bash
# Deploy microservices to VM (veritascampus.me). Run from repo root on the server.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

if [[ -d .git ]]; then
  echo "==> Pulling latest code..."
  git fetch origin
  git pull --ff-only origin "$(git rev-parse --abbrev-ref HEAD)"
fi

if [[ ! -f .env ]]; then
  echo "Missing .env — create from .env.example with SES and domain settings."
  exit 1
fi

DOCKER="docker"
if ! docker info >/dev/null 2>&1; then
  DOCKER="sudo docker"
fi

echo "==> Building and starting microservices..."
$DOCKER compose --env-file .env up -d --build

echo "==> Waiting for gateway..."
sleep 15

echo "==> Building frontend..."
cd frontend
npm ci
npm run build

echo "==> Publishing frontend..."
sudo mkdir -p /var/www/veritascampus-me
sudo rsync -a --delete dist/ /var/www/veritascampus-me/

echo "==> Installing nginx site..."
sudo cp "$ROOT/docs/nginx-veritascampus-me.conf" /etc/nginx/sites-available/veritascampus-me
sudo ln -sf /etc/nginx/sites-available/veritascampus-me /etc/nginx/sites-enabled/veritascampus-me
sudo nginx -t
sudo systemctl reload nginx

echo "Done. Test: curl -I http://127.0.0.1/"
